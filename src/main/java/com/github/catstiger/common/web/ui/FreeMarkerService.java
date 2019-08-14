package com.github.catstiger.common.web.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.github.catstiger.common.util.Exceptions;
import com.google.common.base.Charsets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 执行freemarker模板操作的辅助类，子类需要注入这个类才能使用。简化了freemarker
 * 模板操作的异常处理并且对于DataModel为Collection,Array,Number,String,Date的情况作了处理。
 * 
 * @author catstiger@gmail.com
 */
@Component
@Lazy
public class FreeMarkerService {
  protected Configuration configuration;

  @Value("${app.freemarker.templateDirectory}")
  private String templateDirectory;

  @PostConstruct
  public void init() throws Exception {
    this.configuration = freemarkerConfig();
  }

  private Configuration freemarkerConfig() throws TemplateException, IOException {
    Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    config.setDefaultEncoding(Charsets.UTF_8.name());
    config.setSetting("date_format", "yyyy-MM-dd");
    config.setSetting("datetime_format", "yyyy-MM-dd HH:mm:ss");
    // 如果以classpath:开头，则删除这个前缀
    if (StringUtils.startsWith(templateDirectory, ResourceUtils.CLASSPATH_URL_PREFIX)) {
      templateDirectory = templateDirectory.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
    }
    config.setClassLoaderForTemplateLoading(getClass().getClassLoader(), templateDirectory);
    return config;
  }

  /**
   * 加载指定模板，并结合给出的数据将模板解析为String.
   * 
   * @param templateName 模板名称，例如 myTemplate.ftl。建议是唯一的，否则没准儿加载哪个。
   * @param model        用于填充模板的数据。
   *                     <ul>
   *                     <li>如果数据类型是Map或者JavaBean,则直接写入模板，模板中可以直接使用他们的属性或者Entry。</li>
   *                     <li>如果数据类型是List或者Array，则以"list"作为Key写入模板.</li>
   *                     <li>如果数据类型是Number或者String，则以"data"作为Key写入模板.</li>
   *                     <li>如果数据类型是Date，则以"dt"作为Key写入模板.</li>
   *                     </ul>
   * 
   * @return the result as String
   * @throws RuntimeException if any exception occurs.
   */
  public String processTemplate(String templateName, Object model) {
    try {
      Template template = configuration.getTemplate(templateName);
      if (model != null) {
        Map<String, Object> dataModel = new HashMap<String, Object>();
        if (model instanceof Collection<?> || model.getClass().isArray()) {
          dataModel.put("list", model);
          model = dataModel;
        } else if (model instanceof Number || model instanceof String) {
          dataModel.put("data", model);
          model = dataModel;
        } else if (model instanceof Date) {
          dataModel.put("dt", model);
          model = dataModel;
        }
      }
      return processTemplateIntoString(template, model);
    } catch (IOException e) {
      e.printStackTrace();
      throw Exceptions.unchecked(e);
    } catch (TemplateException e) {
      e.printStackTrace();
      throw Exceptions.unchecked(e);
    }
  }

  /**
   * Process the specified FreeMarker template with the given model and write the
   * result to the given Writer. When using this method to prepare a text for a
   * mail to be sent with Spring's mail support, consider wrapping
   * IO/TemplateException in MailPreparationException.
   * 
   * @param model the model object, typically a Map that contains model names as
   *              keys and model objects as values
   * @return the result as String
   * @throws IOException       if the template wasn't found or couldn't be read
   * @throws TemplateException if rendering failed
   * @see org.springframework.mail.MailPreparationException
   */
  public static String processTemplateIntoString(Template template, Object model)
      throws IOException, TemplateException {
    StringWriter result = new StringWriter();
    template.process(model, result);

    return result.toString();
  }

}
