package com.lin.fileupload.controller;

import com.lin.fileupload.entity.Student;
import com.lin.fileupload.utils.ImportExcelUtils;
import com.lin.fileupload.utils.ExportExcelUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Created by baozang Cotter on 2018/11/30.
 */
@RestController
public class ExcelController {

    /**
     * 上传多个附件的操作类
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String doUploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        //heml利用表单上传文件
        //<form enctype="multipart/form-data" method="post" action="http://127.0.0.1:9999/upload"/>
        //MultipartFile类表示HTML用form-data方式上传的文件
        //一般只有包括二进制数据和文件名称
        //File类表示系统中文件的抽象
        if (!file.isEmpty()) {
            try {
                // copyInputStreamToFile将上传得到的文件保存指定目录下
                FileUtils.copyInputStreamToFile(file.getInputStream(),
                        new File("d:\\testImg\\file\\", (UUID.randomUUID().toString()) + file.getOriginalFilename()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //文件数据存储
        List<List<Object>> listob = null;
        //获取文件数据
        InputStream in = file.getInputStream();
        listob = new ImportExcelUtils().getBankListByExcel(in, file.getOriginalFilename());

        for(List<Object> list : listob) {
            for(Object o : list){
                System.out.print(o.toString());
                System.out.print("***");
            }
            System.out.println("");
        }

        return "success"; // 上传成功则跳转至此success的信息
    }


    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //request.setCharacterEncoding()作用：用指定的编码集去覆盖request对象中的默认的"ISO-8859-1"编码集，必须在第一次使用request指定，否则不生效。
        request.setCharacterEncoding("UTF-8");
        //response.setContentType()作用：使客户端浏览器，区分不同种类的数据，并根据不同的MIME调用浏览器内不同的程序嵌入模块来处理相应的数据。
        // text/html:HTML格式    text/plain:纯文本格式     text/xml:XML格式
        response.setContentType("text/html;charset=UTF-8");

        //程序输入流和输出流，缓冲区处理流
//        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        //getSession()表示获取Session对象，getServletContext()表示获取servlet容器对象（也就是tomcat对象）
        //getRealPath()表示获取在项目目录的下，文件的存储路径
//        String path = request.getSession().getServletContext().getRealPath("/resources/");
        //输入的文件名字
        String fileName = "人员信息表.xls";
        fileName = new String(fileName.getBytes(), "ISO8859-1");
        try {
//            File f = new File(path + fileName);
            response.setContentType("application/ms-excel");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
//            response.setHeader("Content-Length", String.valueOf(f.length()));

            //获取响应输出流
            out = new BufferedOutputStream(response.getOutputStream());

            String title = "人员信息";
            String[] headers = {"名字","性别","年龄","城市"};

            Collection<Object> list = new ArrayList<>();
            list.add(new Student("冉文婷","女",23,"乌鲁木齐"));
            list.add(new Student("林藏宝","男",20,"泉州"));
            list.add(new Student("吴艳玲","女",24,"晋江"));

            ExportExcelUtils.exportExcel(title,headers,list,out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }


}
