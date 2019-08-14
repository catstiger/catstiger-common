package com.github.catstiger.common.poi;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.catstiger.common.sql.BaseEntity;
import com.github.catstiger.common.sql.JdbcTemplateProxy;
import com.github.catstiger.common.sql.SQLReady;
import com.github.catstiger.common.sql.SQLRequest;
import com.github.catstiger.common.sql.id.IdGen;
import com.github.catstiger.common.util.Exceptions;
import com.github.catstiger.common.util.IOHelper;
import com.github.catstiger.common.util.ReflectUtil;
import com.google.common.base.CaseFormat;

@Component
public class ExcelHandler implements ApplicationContextAware {
  private static Logger logger = LoggerFactory.getLogger(ExcelHandler.class);
  
  private ApplicationContext context;
  @Autowired
  private IdGen idGen;

  @Override
  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

  /**
   * 根据Cell标注，导入Excel数据
   * @param in Excel InputStream
   * @param entityClass 对应的实体类Class, 必须是{@code BaseEntity} 的子类
   * @param isXlsx 是否是.xlsx文件
   */
  @Transactional
  public List<RowValid> importExcel(InputStream in, Class<?> entityClass, boolean isXlsx) {
    List<RowValid> entities = Collections.emptyList();
    
    try (Workbook workbook = (isXlsx ? new XSSFWorkbook(in) : new HSSFWorkbook(in))) {
      Sheet sheet = workbook.getSheetAt(0);
      entities = new ArrayList<>(sheet.getLastRowNum());
      
      Row firstRow = sheet.getRow(0);

      if (firstRow == null) {
        return Collections.emptyList();
      }
      
      List<FieldCell> fieldCells = this.parseAnySort(entityClass, firstRow); //解析Excel和Entity的对应关系
      
      for(Iterator<Row> itr = sheet.iterator(); itr.hasNext();) {
        Row row = itr.next();
        if (row.getRowNum() == 0) {
          continue;
        }
        
        RowValid rowValid = buildInstance(row, fieldCells, entityClass);
        if (rowValid != null) {
          if (rowValid.getEntity() != null) {
            saveEntity(entityClass, (BaseEntity) rowValid.getEntity()); //保存到数据库
          }
          entities.add(rowValid);
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      IOHelper.closeQuietly(in);
    }
    
    return entities;
  }
  
  /**
   * 总结导入情况
   */
  public Summary summary(List<RowValid> results) {
    Summary sum = new Summary();
    sum.setTotal(results.size());
    for(RowValid rowValid : results) {
      sum.addInvalid(rowValid);
    }
    sum.setErrors(sum.getRowValids().size());
    sum.setSuccess(sum.getErrors() == 0);
    return sum;
  }
  
  /**
   * 保存单个Entity实例
   */
  @Transactional
  private BaseEntity saveEntity(Class<?> entityClass, BaseEntity entity) {
    JdbcTemplateProxy jdbcTemplate = context.getBean(JdbcTemplateProxy.class);
    SQLReady sqlReady = new SQLRequest(entityClass, true).entity(entity).insertNonNull();
    jdbcTemplate.update(sqlReady.getSql(), sqlReady.getArgs());
    
    return entity;
  }
  
  /**
   * 根据单行数据，构建entity实例
   * @param row 给出Excel Role
   * @param fieldCells Excel和entity的对应关系
   * @param entityClass 实体类class
   * @return
   */
  private RowValid buildInstance (Row row, List<FieldCell> fieldCells, Class<?> entityClass) {
    BaseEntity entity = (BaseEntity) ReflectUtil.instantiate(entityClass);
    entity.setId(idGen.nextId());
    
    for(FieldCell fieldCell : fieldCells) {
      if (fieldCell.ignore) {
        continue;
      }
      org.apache.poi.ss.usermodel.Cell cell = row.getCell(fieldCell.order);
      Class<?> requiredClass = fieldCell.field.getType();
     
      //获取数据并转换
      Object value;
      if (fieldCell.converter != null) {
        value = fieldCell.converter.convert(cell);
      } else {
        value = POIUtil.getValue(cell, requiredClass);
      }
      
      //验证
      if (fieldCell.validator != null) {
        CellValid valid = fieldCell.validator.validate(cell, value);
        if (!valid.isValid()) {
          logger.error("导入验证失败: {}", valid.getErrorMessage());
          return new RowValid(false, valid.getErrorMessage());
        }
      }
      
      ReflectUtil.setField(fieldCell.field, entity, value);
    }
    
    return new RowValid(row.getRowNum(), entity);
  }
  
  /**
   * 解析EntityClass，根据Field上标注的{@code Cell}
   */
  @SuppressWarnings("unchecked")
  private List<FieldCell> parseClass(Class<?> entityClass) {
    Field[] fields = ReflectUtil.getFields(entityClass);
    List<FieldCell> fieldCells = new ArrayList<>(fields.length);
    
    for(Field field : fields) {
      Cell cellAnn = field.getAnnotation(Cell.class);
      if (cellAnn == null) {
        continue;
      }
      FieldCell fieldCell = new FieldCell();
      fieldCell.setField(field);
      String underLineCol = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
      fieldCell.setCol(StringUtils.isBlank(cellAnn.columnName()) ? underLineCol : cellAnn.columnName());
      fieldCell.setIgnore(cellAnn.ignore());
      fieldCell.setTitle(cellAnn.title());
      
      //转换器
      Class<CellConverter> converterClass = (Class<CellConverter>) cellAnn.converter();
      CellConverter converter = null;
      if (!NothingConverter.class.equals(converterClass)) {
        if (!context.containsBean(StringUtils.uncapitalize(converterClass.getSimpleName()))) {
          converter = ReflectUtil.instantiate(converterClass);
        } else {
          converter = context.getBean(converterClass);
        }
        fieldCell.setConverter(converter);
      }
      
      //验证器
      Class<CellValidator> validatorClass = (Class<CellValidator>) cellAnn.validator();
      CellValidator validator = null;
      if (!NothingValidator.class.equals(validatorClass)) {
        if (!context.containsBean(StringUtils.uncapitalize(validatorClass.getSimpleName()))) {
          validator = ReflectUtil.instantiate(validatorClass);
        } else {
          validator = context.getBean(validatorClass);
        }
        fieldCell.setValidator(validator);
      }
      
      fieldCells.add(fieldCell);
    }
    return fieldCells;
  }
  
  /**
   * 根据Class的描述和首行的Title，确定顺序
   */
  private List<FieldCell> parseAnySort(Class<?> entityClass, Row firstRow) {
    List<FieldCell> fieldCells = this.parseClass(entityClass);
    int order = 0;
    for(Iterator<org.apache.poi.ss.usermodel.Cell> itr = firstRow.cellIterator(); itr.hasNext();) {
      org.apache.poi.ss.usermodel.Cell cell = itr.next();
      
      if (!cell.getCellType().equals(CellType.STRING)) {
        throw Exceptions.unchecked("首行各个单元格格式必须为字符串。");
      }
      
      String title = cell.getStringCellValue();
      for(FieldCell fc : fieldCells) {
        if (StringUtils.equals(fc.getTitle(), title)) {
          fc.setOrder(order);
          break;
        }
      }
      order ++;
    }
    
    Collections.sort(fieldCells);
    return fieldCells;
  }
  
  /**
   * 描述Field和Cell的关系
   */
  public static class FieldCell implements Comparable<FieldCell> {
    private Field field;
    private String col;
    private String title;
    private boolean ignore;
    private CellConverter converter;
    private CellValidator validator;
    private int order = 0;
    private Object value = null;
    
    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }


    public Field getField() {
      return field;
    }

    public void setField(Field field) {
      this.field = field;
    }

    public String getCol() {
      return col;
    }

    public void setCol(String col) {
      this.col = col;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public boolean isIgnore() {
      return ignore;
    }

    public void setIgnore(boolean ignore) {
      this.ignore = ignore;
    }

    public CellConverter getConverter() {
      return converter;
    }

    public void setConverter(CellConverter converter) {
      this.converter = converter;
    }
    
    public CellValidator getValidator() {
      return validator;
    }

    public void setValidator(CellValidator validator) {
      this.validator = validator;
    }

    @Override
    public int compareTo(FieldCell o) {
      int x = this.order;
      int y = o.order;
      
      return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

  }

}
