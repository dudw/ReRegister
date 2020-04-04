package jetbrains;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class JetBrainsMain {

    //֧�ֵĲ�Ʒ
//    private static final String[] productNames = new String[]{".WebStorm", ".IntelliJIdea"};


    private static final ProductConfig[] products = new ProductConfig[]{
            new ProductConfig(".WebStorm", "webstorm"),
            new ProductConfig(".IntelliJIdea", "idea")
    };


    //�û�Ŀ¼
    private final static String UserProFile = System.getenv("USERPROFILE");


    public static void main(String[] args) {

        JFrame jf = new JFrame("jetbrains");
        jf.setSize(240, 320);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setLocationRelativeTo(null);

        // ����������壬ָ��ʹ�� ��ʽ����
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 5));

        //ɨ�貢����
        scan(panel);

        jf.setContentPane(panel);
        jf.setVisible(true);        // PS: ���������Ϊ����ʾ(����), ������ӵ�����Ż���ʾ
    }


    /**
     * @param panel
     */
    private static void scan(JPanel panel) {
        File file = new File(UserProFile);
        for (String fileName : file.list()) {
            for (final ProductConfig product : products) {
                String productName = product.getMatchName();
                if (fileName.length() < productName.length()) {
                    continue;
                }

                //�ҵ�ע���ļ�
                if (fileName.substring(0, productName.length()).equals(productName)) {
                    File targetFIle = new File(file.getAbsolutePath() + "/" + fileName + "/config/eval");
                    if (targetFIle.exists() && targetFIle.list().length > 0) {
                        JButton button = new JButton();
                        button.setText(fileName);
                        button.addActionListener(new ButtonClick(button, targetFIle, product));
                        panel.add(button);
                    }
                }

            }
        }
    }


    /**
     * ��ť
     */
    static class ButtonClick implements ActionListener {

        private JButton button;
        private File registerFile;
        private ProductConfig product;


        public ButtonClick(JButton button, File registerFile, ProductConfig product) {
            this.button = button;
            this.registerFile = registerFile;
            this.product = product;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(null, "remove " + "[" + button.getText() + "]" + " ? ", "ReRegister", JOptionPane.YES_NO_OPTION);
            if (option == 0) {

                //ɾ����Ŀ¼�������ļ�
                for (File file : registerFile.listFiles()) {
                    file.deleteOnExit();
                }

                // ���ע���
                runCmd("reg delete \"HKEY_CURRENT_USER\\Software\\JavaSoft\\Prefs\\jetbrains\\" + product.getRegeditName() + "\" /f");

                //���ص�ǰ��ť
                button.setVisible(false);

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


    static class ProductConfig {

        /**
         * ƥ������
         */
        private String matchName;

        /**
         * ע�������
         */
        private String regeditName;


        public String getMatchName() {
            return matchName;
        }

        public String getRegeditName() {
            return regeditName;
        }

        public ProductConfig() {
        }

        public ProductConfig(String matchName, String regeditName) {
            this.matchName = matchName;
            this.regeditName = regeditName;
        }
    }

}
