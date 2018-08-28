package com.git.utils;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author wangquan
 * 从properties中读取国际化
 *
 */
@Component
public class MessageUtil {
	private static MessageSource messageSource;
	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		MessageUtil.messageSource = messageSource;
	}
	
	public static String getMessage(String code, Object[] args, Locale locale) {
        Assert.notNull(messageSource, "MessageSourceUtil not init ...");
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return null;
        }
    }
}
