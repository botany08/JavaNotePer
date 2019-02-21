package com.monster.lin.mvc.person.blog.servlet.sessionTest;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * 类 <code>{类名称}</code>{此类功能描述}
 *
 * @author zangbao.lin
 * @version 2019/2/21
 * @since JDK 1.8
 */
@WebServlet(name = "imageServlet",urlPatterns = {"/imageServlet"})
public class ImageServlet extends HttpServlet {
    private static int height = 30;
    private static int weighth = 4*20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.生成验证码图片
        BufferedImage bufferedImage = new BufferedImage(weighth,height,BufferedImage.TYPE_INT_RGB);

        //2.绘制验证码图片
        String verificationCode = drawImage(bufferedImage);

        //3.将验证码存入session
        req.getSession().setAttribute("veriCode",verificationCode);

        //4.设置返回信息头，不缓存以及图片格式
        resp.setHeader("Expires", "-1");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Content-type", "image/jpeg");

        //5.将图片写到浏览器
        ImageIO.write(bufferedImage,"jpg",resp.getOutputStream());

    }

    private String drawImage(BufferedImage bufferedImage) {
        Random rand = new Random();

        //获取对图片操作权
        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();

        //填充背景
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0,0,weighth,height);

        //设置字体
        Font font = new Font("微软雅黑",Font.PLAIN,height-5);
        graphics2D.setFont(font);

        //绘制边框
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawRect(0,0,weighth-1,height-1);

        //绘制灰色干扰线
        graphics2D.setColor(Color.GRAY);
        for (int i = 0, x, y, x1, y1; i < 30; i++) {
            x = rand.nextInt(weighth);
            y = rand.nextInt(height);
            x1 = rand.nextInt(12);
            y1 = rand.nextInt(12);
            graphics2D.drawLine(x, y, x + x1, y + y1);
        }

        //生成验证码
        StringBuffer verificationCode = new StringBuffer();

        //验证码选择范围
        String tmp;
        char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

        for(int i = 0, red, green, blue; i < 4;i++) {
            //随机获取字符
            tmp = String.valueOf(codeSequence[rand.nextInt(codeSequence.length)]);

            //随机选择字符颜色
            red = rand.nextInt(255);
            green = rand.nextInt(255);
            blue = rand.nextInt(255);
            graphics2D.setColor(new Color(red,green,blue));

            //绘制字符
            graphics2D.drawString(tmp,(i+1)*15,25);

            //记录字符
            verificationCode.append(tmp);
        }

        return verificationCode.toString();

    }
}
