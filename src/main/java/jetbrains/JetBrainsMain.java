package jetbrains;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class JetBrainsMain {


    private static final ProductConfig[] products = new ProductConfig[]{

            new ProductConfig(System.getenv("USERPROFILE"), ".WebStorm", "config/eval", "webstorm"),
            new ProductConfig(System.getenv("appdata") + "/JetBrains", "WebStorm", "eval", "webstorm"),

            new ProductConfig(System.getenv("USERPROFILE"), ".IntelliJIdea", "config/eval", "idea"),
            new ProductConfig(System.getenv("appdata") + "/JetBrains", "IntelliJIdea", "eval", "idea"),
			
			
			new ProductConfig(System.getenv("appdata") + "/JetBrains", "PyCharm", "eval", "pycharm"),
			
    };


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
        for (ProductConfig product : products) {
            loadProduct(product, panel);
        }


    }

    private static void loadProduct(ProductConfig product, JPanel panel) {
        File file = new File(product.getPath());
        for (String fileName : file.list()) {
            String productName = product.getMatchNames();
            if (fileName.length() < productName.length()) {
                continue;
            }
            //�ҵ�ע���ļ�
            if (fileName.substring(0, productName.length()).equals(productName)) {
                File targetFIle = new File(file.getAbsolutePath() + "/" + fileName + "/" + product.getProductFolder());
                if (targetFIle.exists() && targetFIle.list().length > 0) {
                    JButton button = new JButton();
                    button.setText(fileName);
                    button.addActionListener(new ButtonClick(button, targetFIle, product));
                    panel.add(button);
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
                    file.delete();
                }

                // ɾ�� other.xml
                new File(registerFile.getParent() + "/options/other.xml").delete();


                runCmd("reg delete \"HKEY_CURRENT_USER\\Software\\JavaSoft\\Prefs\\jetbrains\" /va /f");

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

        /***
         * ·��
         */
        private String path;


        /**
         * ƥ������
         */
        private String matchNames;

        /**
         * ע����Ϣ��Ŀ¼
         */
        private String productFolder;

        /**
         * ע�������
         */
        private String regeditName;

        public String getProductFolder() {
            return productFolder;
        }

        public String getPath() {
            return path;
        }

        public String getMatchNames() {
            return matchNames;
        }

        public String getRegeditName() {
            return regeditName;
        }

        public ProductConfig(String path, String matchNames, String productFolder, String regeditName) {
            this.path = path;
            this.matchNames = matchNames;
            this.productFolder = productFolder;
            this.regeditName = regeditName;
        }
    }


}
