package com.git.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.git.resp.BaseResponse;
import com.google.gson.Gson;

public class GlobeExceptionResolver implements HandlerExceptionResolver{
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,Exception ex) {
		//打印错误
		ex.printStackTrace();
		
		//进行返回
		if (request.getHeader("x-requested-with") != null&& request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {
			return this.processAjax(response);
		}else{
			return this.processNormal(response);
		}
	}
	
	
	/**
	 * 针对ajax错误的返回
	 * @param response
	 * @return
	 */
	private ModelAndView processAjax(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store");
		try {
			response.getWriter().write(new Gson().toJson(BaseResponse.FAIL()));
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ModelAndView();
	}
	/**
	 * 针对普通请求的返回
	 * @param response
	 * @return
	 */
	private ModelAndView processNormal(HttpServletResponse response) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("msg", "出现异常了");
		modelAndView.setViewName("500");
		return modelAndView;
	}
}