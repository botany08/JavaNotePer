package com.lin.fileupload.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by baozang Cotter on 2018/12/4.
 */
public class ExportExcelUtils {
    /**
     * @param title
     *            工作表名
     * @param headers
     *            表格属性列名数组
     * @param dataset
     *            需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *            javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据)
     * @param out
     *            与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern
     *            如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     */
    public static void exportExcel(String title, String[] headers, Collection<Object> dataset, OutputStream out)
    {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 15);

        // 生成并设置一个样式
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        // 生成一个字体
        Font font = workbook.createFont();
        font.setColor(IndexedColors.VIOLET.getIndex());
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        // 把字体应用到当前的样式
        style.setFont(font);

        // 生成并设置另一个样式
        HSSFCellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);
        // 生成另一个字体
        HSSFFont font2 = workbook.createFont();
        font2.setBold(true);
        // 把字体应用到当前的样式
        style2.setFont(font2);


        // 声明一个画图的顶级管理器-设置单元格的批注内容
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        // 定义注释的大小和位置,详见文档
        HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0,
                0, 0, 0, (short) 4, 2, (short) 6, 5));
        // 设置注释内容
        comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));
        // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.
        comment.setAuthor("leno");

        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);   //创建第一行
        for (short i = 0; i < headers.length; i++)
        {
            HSSFCell cell = row.createCell(i);  //创建单元格
            cell.setCellStyle(style);      //设置单元格风格
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);   //单元格文本类（富文本意思是可以有文本，颜色，格式）
            cell.setCellValue(text);       //插入单元格内容
        }

        // 遍历集合数据，产生数据行
        Iterator<Object> it = dataset.iterator();
        int index = 0;
        while (it.hasNext())
        {
            index++;    //跳过标题行
            row = sheet.createRow(index); //创建内容第一行
            Object t = it.next();    //获取bean对象
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            Field[] fields = t.getClass().getDeclaredFields();  //获取类所有属性对象
            for (short i = 0; i < fields.length; i++)
            {
                HSSFCell cell = row.createCell(i);  //创建单元格
                cell.setCellStyle(style2);      //设置单元格格式
                Field field = fields[i];        //获取具体属性对象
                String fieldName = field.getName(); //属性名称
                String getMethodName = "get"
                        + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);       //例如：name 转换为 getName
                try
                {
                    Method getMethod = t.getClass().getMethod(getMethodName, new Class[]{}); //获取get方法对象
                    Object value = getMethod.invoke(t, new Object[]{});     //调用get方法获取属性值

                    // 判断值的类型后进行强制类型转换
                    String textValue = null;

                    if (value instanceof Boolean)   //true为男，false为女
                    {
                        boolean bValue = (Boolean) value;
                        textValue = "男";
                        if (!bValue)
                        {
                            textValue = "女";
                        }
                    }
                    else if (value instanceof Date) //pattern表示日期格式
                    {
                        Date date = (Date) value;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        textValue = sdf.format(date);
                    }
                    else if (value instanceof byte[])   //字节数组-图片
                    {
                        // 有图片时，设置行高为60px;
                        row.setHeightInPoints(60);
                        // 设置图片所在列宽度为80px,注意这里单位的一个换算
                        sheet.setColumnWidth(i, (short) (35.7 * 80));
                        // sheet.autoSizeColumn(i);
                        byte[] bsValue = (byte[]) value;
                        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0,
                                1023, 255, (short) 6, index, (short) 6, index);
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                        patriarch.createPicture(anchor, workbook.addPicture(
                                bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));
                    }
                    else
                    {
                        // 其它数据类型都当作字符串简单处理
                        textValue = value.toString();
                    }

                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
                    if (textValue != null)
                    {
                        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                        Matcher matcher = p.matcher(textValue);
                        if (matcher.matches())
                        {
                            // 是数字当作double处理
                            cell.setCellValue(Double.parseDouble(textValue));
                        }
                        else
                        {
                            HSSFRichTextString richString = new HSSFRichTextString(textValue);//转换为文本
                            HSSFFont font3 = workbook.createFont();
                            font3.setColor(IndexedColors.BLUE.getIndex());
                            richString.applyFont(font3);    //添加字体
                            cell.setCellValue(richString);
                        }
                    }
                }
                catch (SecurityException e)
                {
                    e.printStackTrace();
                }
                catch (NoSuchMethodException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    // 清理资源
                }
            }
        }
        try
        {
            //将文件数据写入到输出流
            workbook.write(out);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
