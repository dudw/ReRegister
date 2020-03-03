package studio3t;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Studio3TMain {
    public static void main(String[] args) {
        JFrame jf = new JFrame("Studio-3T");
        jf.setSize(240, 320);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setLocationRelativeTo(null);

        // ����������壬ָ��ʹ�� ��ʽ����
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 5));


        JButton button = new JButton();
        button.setText("Reset Studio-3T");
        button.addActionListener(new ButtonClick());
        panel.add(button);


        jf.setContentPane(panel);
        jf.setVisible(true);        // PS: ���������Ϊ����ʾ(����), ������ӵ�����Ż���ʾ
    }


    static class ButtonClick implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(null, "remove " + "[Studio-3T]" + " ? ", "ReRegister", JOptionPane.YES_NO_OPTION);
            if (option == 0) {
                //ע���
                runCmd("reg delete \"HKEY_CURRENT_USER\\Software\\JavaSoft\\Prefs\\3t\\mongochef\\enterprise\" /f");

                //�û��ļ�
                runCmd("rd /s /q %USERPROFILE%\\AppData\\Local\\ftuwWNWoJl-STeZhVGHKkQ--");
                runCmd("rd /s /q %USERPROFILE%\\AppData\\Local\\t3");
                runCmd("rd /s /q %USERPROFILE%\\AppData\\Local\\Temp\\ftuwWNWoJl-STeZhVGHKkQ--");
                runCmd("rd /s /q %USERPROFILE%\\AppData\\Local\\Temp\\t3");
                runCmd("rd /s /q %PUBLIC%\\t3");
                runCmd("rd /s /q %USERPROFILE%\\.cache");
                runCmd("rd /s /q %USERPROFILE%\\.3T\\studio-3t\\soduz3vqhnnja46uvu3szq--");

                JOptionPane.showMessageDialog(null,"finish");
            }
        }
    }

    static void runCmd(String cmd) {
        try {
            Runtime.getRuntime().exec("cmd /c " + cmd);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
