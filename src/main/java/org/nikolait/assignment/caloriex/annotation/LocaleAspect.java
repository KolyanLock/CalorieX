package org.nikolait.assignment.caloriex.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Aspect
@Component
public class LocaleAspect {

    @Before("@within(LocaleContext)")
    public void setLocaleContext(JoinPoint joinPoint) {
        LocaleContext annotation = joinPoint.getTarget().getClass().getAnnotation(LocaleContext.class);
        if (annotation != null) {
            LocaleContextHolder.setLocale(Locale.of(annotation.value()));
        }
    }

}
