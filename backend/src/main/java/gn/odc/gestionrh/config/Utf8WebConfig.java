package gn.odc.gestionrh.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class Utf8WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> utf8CharacterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding(StandardCharsets.UTF_8.name());
        filter.setForceEncoding(true);
        FilterRegistrationBean<CharacterEncodingFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(0);
        return bean;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(1, json);
    }
}
