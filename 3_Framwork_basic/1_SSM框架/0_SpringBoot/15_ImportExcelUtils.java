package com.lin.fileupload.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baozang Cotter on 2018/11/30.
 */
public class ImportExcelUtils {
    private final static String excel2003L =".xls";    //2003- 版本的excel
    private final static String excel2007U =".xlsx";   //2007+ 版本的excel

    /**
     * 描述：获取IO流中的数据，组装成List<List<Object>>对象
     * @param in,fileName
     * @return
     * @throws IOException
     */
    public  List<List<Object>> getBankListByExcel(InputStream in, String fileName) throws Exception{
        List<List<Object>> list = null;

        //创建Excel工作薄对象
        Workbook work = this.getWorkbook(in,fileName);
        if(null == work){
            throw new Exception("创建Excel工作薄为空！");
        }
        Sheet sheet = null;     //列表页
        Row row = null;         //行
        Cell cell = null;       //列

        //用来存储数据的对象
        list = new ArrayList<List<Object>>();
        //遍历Excel中所有的sheet
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            //获取当前sheet
            sheet = work.getSheetAt(i);
            //当前sheet为空时，continue
            if(sheet==null){continue;}
            //遍历当前sheet中的所有行
            for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                //获取当前行
                row = sheet.getRow(j);
                if(row==null){continue;}
                //遍历当前行的所有的列
                List<Object> li = new ArrayList<Object>();
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    //getCellValue()表示获取单元格的值
                    li.add(this.getCellValue(cell));
                }
                list.add(li);
            }
        }
        work.close();
        return list;
    }

    /**
     * 描述：根据文件后缀，自适应上传文件的版本
     * @param inStr,fileName
     * @return
     * @throws Exception
     */
    public  Workbook getWorkbook(InputStream inStr,String fileName) throws Exception{
        Workbook wb = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if(excel2003L.equals(fileType)){
            wb = new HSSFWorkbook(inStr);  //2003-
        }else if(excel2007U.equals(fileType)){
            wb = new XSSFWorkbook(inStr);  //2007+
        }else{
            throw new Exception("解析的文件格式有误！");
        }
        return wb;
    }

    /**
     * 描述：对表格中数值进行格式化
     * @param cell
     * @return
     */
    public  Object getCellValue(Cell cell){
        Object value = null;
        //DecimalFormat类为数字格式化类
        DecimalFormat df = new DecimalFormat("0");  //格式化number String字符
        //SimpleDateFormat类为日期格式化类
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");  //日期格式化
        DecimalFormat df2 = new DecimalFormat("0.00");  //格式化数字

        //判断单元格内容的类型
        switch (cell.getCellTypeEnum()) {
            //字符串
            case STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            //数字类型
            case NUMERIC:
                //常规格式
                if("General".equals(cell.getCellStyle().getDataFormatString())){
                    value = df.format(cell.getNumericCellValue());
                //日期格式
                }else if("m/d/yy".equals(cell.getCellStyle().getDataFormatString())){
                    value = sdf.format(cell.getDateCellValue());
                //除此之外都默认为数字保留两个小数点
                }else{
                    value = df2.format(cell.getNumericCellValue());
                }
                break;
            //布尔类型
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            //空白字符串
            case BLANK:
                value = "";
                break;
            default:
                break;
        }
        return value;
    }


}
