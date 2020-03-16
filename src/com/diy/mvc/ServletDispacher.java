package com.diy.mvc;

import com.diy.annotations.MyAutoWired;
import com.diy.annotations.MyController;
import com.diy.annotations.MyRequestMapping;
import com.diy.annotations.MyService;
import com.sun.deploy.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServletDispacher extends HttpServlet {
    private Properties properties = new Properties();
    private List<Class<?>> classes = new ArrayList();
    private Map<String, Object> ioc = new HashMap();
    private Map<String, Method> urlMap = new HashMap();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestURI = req.getRequestURI();
        requestURI = requestURI.substring(req.getContextPath().length());
        Method method = urlMap.get(requestURI);
        if (method == null) {
            resp.getWriter().write("404");
        } else {

            try {
                resp.getWriter().write(method.invoke(ioc.get(firstCodeToLower(method.getDeclaringClass().getSimpleName())), new Object[]{}).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        loadConfig(config);
        scanPackages(this.getClass().getClassLoader().getResource("").getFile(), properties.getProperty("mvc.package"));
        initAllBeans();
        autoWiredBean();
        initHandleMapping();
    }

    private void initHandleMapping() {
        for (Object value : ioc.values()) {
            if (!value.getClass().isAnnotationPresent(MyController.class)) {
                continue;
            }
            MyRequestMapping annotation = value.getClass().getAnnotation(MyRequestMapping.class);
            String url = annotation != null && !"".equals(annotation.value()) ? annotation.value() : "";

            for (Method method : value.getClass().getDeclaredMethods()) {
                MyRequestMapping requestMapping = method.getDeclaredAnnotation(MyRequestMapping.class);
                if (requestMapping == null) {
                    continue;
                }
                String methodUrl = url + "/" + requestMapping.value();
                if (urlMap.containsKey(methodUrl)) {
                    System.out.println("url:" + methodUrl + "  已存在！！");
                    continue;
                }
                method.setAccessible(true);

                urlMap.put(methodUrl, method);
            }
        }
    }

    private void autoWiredBean() {
        for (Object value : ioc.values()) {
            for (Field field : value.getClass().getDeclaredFields()) {
                MyAutoWired annotation = field.getAnnotation(MyAutoWired.class);
                if (annotation == null) {
                    continue;
                }
                field.setAccessible(true);
                boolean ifFindByNeedName = !"".equals(annotation.value());

                String beanName = ifFindByNeedName ? annotation.value() : firstCodeToLower(field.getType().getSimpleName());
                Object object = ioc.get(beanName);

                //如果根据beanName没找到，又没指定beanName，则尝试通过class查找
                if (object == null && !ifFindByNeedName) {
                    Class fieldClass = field.getType();
                    List<Object> sameClassBeans = null;
                    if (fieldClass.isInterface()) {
                        sameClassBeans = ioc.values().parallelStream().filter(bean -> Arrays.asList(bean.getClass().getInterfaces()).contains(fieldClass)).collect(Collectors.toList());
                    }

                    if (sameClassBeans == null || sameClassBeans.isEmpty() || sameClassBeans.size() > 1) {
                        System.out.println("该接口实现有好" + sameClassBeans.size() + "个，不知用哪个了：" + beanName);
                        continue;
                    }

                    object = sameClassBeans.get(0);
                }

                //如果配置了beanName又 没有找到，则报错！跳过
                if (object == null) {
                    System.out.println("未找到指定的beanName：" + beanName);
                    continue;
                }
                try {
                    field.set(value, object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }


    private void initAllBeans() {
        for (Class<?> clzz : classes) {
            String beanName = null;
            if (clzz.isAnnotationPresent(MyController.class)) {
                beanName = firstCodeToLower(clzz.getSimpleName());
            } else if (clzz.isAnnotationPresent(MyService.class)) {
                MyService myService = clzz.getDeclaredAnnotation(MyService.class);
                beanName = "".equals(myService.value()) ? firstCodeToLower(clzz.getSimpleName()) : myService.value();
            }

            if (beanName == null) {
                continue;
            }
            try {
                if (ioc.containsKey(beanName)) {
                    System.out.println("bean ：" + beanName + " 已存在！！");
                }
                ioc.put(beanName, clzz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String firstCodeToLower(String word) {
        byte[] bytes = word.getBytes();

        if ('A' <= bytes[0] && bytes[0] <= 'Z') {
            bytes[0] += 32;
            return new String(bytes);
        }
        return word;
    }

    private void scanPackages(String basePath, String packageName) {
        String filePath = basePath + "/" + packageName.replace(".", "/");
        File file = new File(filePath);
        for (File nxFile : file.listFiles()) {
            String fileBasePath = packageName + "." + nxFile.getName();
            if (nxFile.isDirectory()) {
                scanPackages(basePath, fileBasePath);
                continue;
            }
            if (!fileBasePath.endsWith(".class")) {
                continue;
            }
            try {
                classes.add(Class.forName(fileBasePath.substring(0, fileBasePath.length() - 6)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConfig(ServletConfig config) {
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("config")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
