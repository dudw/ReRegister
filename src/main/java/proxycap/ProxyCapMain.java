package proxycap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.attribute.FileStoreAttributeView;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ProxyCapMain {


    private final static String PROXYCAP_INSTALL_URL = "https://download.jpy.wang/proxycap/pcap539_x64.msi";
    private final static File PROXYCAP_WORK = new File(System.getenv("SystemDrive") + "/ProxyCapReset/");
    private final static File PROXYCAP_File = new File(PROXYCAP_WORK.getAbsolutePath() + "/" + PROXYCAP_INSTALL_URL.substring(PROXYCAP_INSTALL_URL.lastIndexOf("/")));
    private final static File PROXYCAP_Backup_Config = new File(System.getenv("SystemDrive") + "/ProxyCapReset/backup/");


    public static void main(String[] args) {
        initFile();

        JFrame jf = new JFrame("ProxyCap");
        jf.setSize(240, 320);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setLocationRelativeTo(null);

        // ����������壬ָ��ʹ�� ��ʽ����
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 5));


        JButton resetProxyCap = new JButton();
        resetProxyCap.setText("���� ProxyCap ����");
        resetProxyCap.addActionListener((e) -> {
            int option = JOptionPane.showConfirmDialog(null, "���� " + "[ProxyCap]" + " ? ", "ReRegister", JOptionPane.YES_NO_OPTION);
            if (option == 0) {
                try {
                    //�ж��Ƿ���Ҫ����
                    downloadProxyCap();

                    //���������ļ�
                    backupConfig();

                    //���ע����Ϣ
                    resetRegInfo();

                    //�޸���װ
                    repairInstall();
                    JOptionPane.showMessageDialog(null, "���������񣬲��ָ������ļ�: \n" + PROXYCAP_Backup_Config.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        panel.add(resetProxyCap);


        JButton uninstallProxyCap = new JButton();
        uninstallProxyCap.setText("ж�� ProxyCap ����");
        uninstallProxyCap.addActionListener((e) -> {
            int option = JOptionPane.showConfirmDialog(null, "ж�� " + "[ProxyCap]" + " ? ", "ReRegister", JOptionPane.YES_NO_OPTION);
            if (option == 0) {
                try {
                    //�ж��Ƿ���Ҫ����
                    downloadProxyCap();

                    //���������ļ�
                    backupConfig();

                    //���ע����Ϣ
                    resetRegInfo();

                    //ж��
                    unInstall();
                    JOptionPane.showMessageDialog(null, "ж�����,�������ļ�: \n" + PROXYCAP_Backup_Config.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        panel.add(uninstallProxyCap);


        JButton stopProxyCap = new JButton();
        stopProxyCap.setText("ֹͣ ProxyCap ����");
        stopProxyCap.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopService();
                JOptionPane.showMessageDialog(null, "����ֹͣ���");
            }
        });
        panel.add(stopProxyCap);


        JButton reStartProxyCap = new JButton();
        reStartProxyCap.setText("���� ProxyCap ����");
        reStartProxyCap.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopService();

                runCmd("net start pcapsvc");
                runCmd("powershell -Command \"$Key = 'HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run' ; $Name = 'ProxyCap' ; $result = (Get-ItemProperty -Path \"Registry::$Key\" -ErrorAction Stop).$Name; & $result\"");
                JOptionPane.showMessageDialog(null, "�����������");
            }
        });
        panel.add(reStartProxyCap);


        jf.setLayout(new BorderLayout());
        jf.add(panel, BorderLayout.CENTER);
//        jf.setContentPane(panel);
        jf.setVisible(true);        // PS: ���������Ϊ����ʾ(����), ������ӵ�����Ż���ʾ

    }

    private static void initFile() {
        if (!PROXYCAP_WORK.exists()) {
            PROXYCAP_WORK.mkdirs();
        }
        if (!PROXYCAP_Backup_Config.exists()) {
            PROXYCAP_Backup_Config.mkdirs();
        }
    }




//    private static void install() {
//        runCmd("cmd /c " + PROXYCAP_File.getAbsolutePath() + " /quiet /uninstall " + PROXYCAP_INSTALL_URL.substring(PROXYCAP_INSTALL_URL.lastIndexOf("/")) + " /norestart");
//    }
//

    private static void stopService() {
        runCmd("net stop pcapsvc");
        runCmd("taskkill /im pcapui.exe /f");
    }

    /**
     * ж��
     */
    private static void unInstall() {
//        runCmd("cmd /c " + PROXYCAP_File.getAbsolutePath() + " /quiet /uninstall " + PROXYCAP_INSTALL_URL.substring(PROXYCAP_INSTALL_URL.lastIndexOf("/")) + " /norestart");
        stopService();
        runCmd("cmd /c " + "msiexec /quiet /uninstall " + PROXYCAP_INSTALL_URL + " /norestart");
    }

    private static void repairInstall() {
        runCmd("cmd /c " + PROXYCAP_File.getAbsolutePath() + " /quiet /norestart");
    }


    //���ע����Ϣ
    private static void resetRegInfo() {
        runCmd("reg delete \"HKEY_LOCAL_MACHINE\\Software\\WOW6432Node\\Proxy Labs\" /f");
        runCmd("reg delete \"HKEY_LOCAL_MACHINE\\Software\\WOW6432Node\\SB\" /f");
        runCmd("reg delete \"HKEY_LOCAL_MACHINE\\System\\ControlSet001\\Services\\pcapsvc\" /f");
        runCmd("reg delete \"HKEY_LOCAL_MACHINE\\System\\ControlSet001\\Services\\Tcpip\\Parameters\\Arp\" /f");
    }


    //��ʼ���������ļ�
    private static void backupConfig() throws Exception {
        File machine_prs_file = new File(System.getenv("ProgramData") + "/ProxyCap/machine.prs");
        if (!machine_prs_file.exists()) {
            System.err.println("config : " + machine_prs_file.getAbsolutePath() + " not exist");
            return;
        }
        try {
            File target_file = new File(PROXYCAP_Backup_Config.getAbsolutePath() + "/" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis())) + "_machine.prs");
            FileOutputStream fileOutputStream = new FileOutputStream(target_file);
            Files.copy(machine_prs_file.toPath(), fileOutputStream);
            fileOutputStream.close();

            System.out.println("config backup success : " + target_file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("config backup error: " + e.getMessage());
        }
    }


    //�жϲ������ļ�
    private static void downloadProxyCap() throws Exception {
        if (PROXYCAP_File.exists() && PROXYCAP_File.length() > 0) {
            return;
        }
        InputStream inputStream = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(new URI(PROXYCAP_INSTALL_URL)).GET().build()
                , HttpResponse.BodyHandlers.ofInputStream()
        ).body();
        FileOutputStream fileOutputStream = new FileOutputStream(PROXYCAP_File);
        byte[] bin = new byte[102400];
        int size = -1;
        long downloadSize = 0;
        while ((size = inputStream.read(bin)) != -1) {
            downloadSize += size;
            fileOutputStream.write(bin, 0, size);
            System.out.println("download : " + downloadSize);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        inputStream.close();
    }

    static void runCmd(String cmd) {
        try {
            Runtime.getRuntime().exec("cmd /c " + cmd).waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * �ַ���
     *
     * @param source
     * @param startText
     * @param endText
     * @return
     */
    static String subText(String source, String startText, String endText, int offSet) {
        int start = source.indexOf(startText, offSet) + 1;
        if (start == -1) {
            return null;
        }
        int end = source.indexOf(endText, start + offSet + startText.length() - 1);
        if (end == -1) {
            end = source.length();
        }
        return source.substring(start + startText.length() - 1, end);
    }

}
