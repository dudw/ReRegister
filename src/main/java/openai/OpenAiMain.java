package openai;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class OpenAiMain {

    // �����ͷ������
    final static String[] columnNames = {"ApiKey", "�ܹ�", "ʣ��"};
    final static Object[][] datas = {};
    // ����Ĭ�ϱ��ģ�ͣ��������ݴ��ݸ���
    final static DefaultTableModel tableModel = new DefaultTableModel(datas, columnNames);


    public static void main(String[] args) {
        JFrame jf = new JFrame("OpenAi - ��ApiKey - ��ק�ı�");
        jf.setSize(800, 600);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setLocationRelativeTo(null);

        // ����������壬ָ��ʹ�� ��ʽ����
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 5));

        // ����ļ���קĿ�������
        new DropTarget(panel, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    // ��ȡ��ק�����еĴ�������
                    Transferable transferable = event.getTransferable();

                    // ����Ƿ����ļ����͵����ݱ�����
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                        // ��ȡ��ק�����е��ļ��б�
                        java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        // �ڱ�ǩ����ʾ��һ���ļ���·��
                        String filePath = fileList.get(0).getAbsolutePath();
                        loadFile(new File(filePath));

                        event.dropComplete(true);
                    } else {
                        event.rejectDrop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    event.rejectDrop();
                }
            }
        });


        // ���� JTable ������������ģ�ʹ��ݸ���
        JTable table = new JTable(tableModel);
        // ��Ӽ����¼���������ʵ�ָ��ƹ���
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
                    // ����Ƿ���ѡ����Ԫ��
                    if (table.getSelectedRowCount() > 0 && table.getSelectedColumnCount() > 0) {
                        StringBuilder sb = new StringBuilder();
                        int[] selectedRows = table.getSelectedRows();
                        for (int row : selectedRows) {
                            sb.append(table.getValueAt(row, 0));
                            if (row > 1) {
                                sb.append(System.lineSeparator());
                            }
                        }

                        // ����ѡ����Ԫ�����ݵ�������
                        StringSelection selection = new StringSelection(sb.toString());
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                    }
                }
            }
        });

        // ��ֹĬ�ϵĸ����¼�
        InputMap inputMap = table.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = table.getActionMap();
        KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(copyKeyStroke, "none");
        actionMap.put("none", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Do nothing
            }
        });

        // �� JList ������ JScrollPane �У��Ա���Թ����鿴�����б���
        JScrollPane scrollPane = new JScrollPane(table);


        // �� JScrollPane ��ӵ� JFrame �����������
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton importButton = new JButton("��������");
        importButton.addActionListener((it) -> {
            // �����ļ�ѡ����
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getenv("USERPROFILE") + "/Desktop"));

            // ��ʾ�ļ�ѡ��Ի��򣬲���ȡ�û�ѡ��Ľ��
            int result = fileChooser.showOpenDialog(jf);
            if (result == JFileChooser.APPROVE_OPTION) {
                // �û�ѡ����һ���ļ�
                java.io.File selectedFile = fileChooser.getSelectedFile();
                loadFile(selectedFile);
            }
        });
        JButton refreshButton = new JButton("ˢ������");
        refreshButton.addActionListener((it) -> {
            refreshDatas();
        });
        panel.add(importButton, BorderLayout.PAGE_START);
        panel.add(refreshButton, BorderLayout.PAGE_END);


        jf.setLayout(new BorderLayout());
        jf.add(panel, BorderLayout.CENTER);
        jf.setVisible(true);
    }


    private static void refreshDatas() {

        //���
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("", i, 1);
            tableModel.setValueAt("", i, 2);
        }


        final Map<Integer, String> items = new ConcurrentHashMap<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String apiKey = (String) tableModel.getValueAt(i, 0);
            if (apiKey == null || apiKey.length() == 0) {
                continue;
            }
            items.put(i, apiKey);
        }


        final CountDownLatch countDownLatch = new CountDownLatch(items.size());

        final Map<Integer, Map<String, Object>> updateItems = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        items.entrySet().forEach((entry) -> {
            executorService.execute(() -> {
                String subscription = get("https://openai.jpy.wang/v1/dashboard/billing/subscription", Map.of("Authorization", "Bearer " + entry.getValue()));
                //�����
                final String system_hard_limit_usd = subText(subscription, "\"system_hard_limit_usd\":", ",", -1).trim();


                //ȡ����ǰʱ��
                Date nowDate = new Date(System.currentTimeMillis());
                // ����һ������ʱ���ʽ����
                final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                // �����ڶ����ʽ��Ϊָ����ʽ���ַ���
                final String end_date = formatter.format(nowDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(nowDate.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, -99);
                final String start_date = formatter.format(calendar.getTime());
                final String query = "start_date=" + start_date + "&end_date=" + end_date;
                String usage = get("https://openai.jpy.wang/v1/dashboard/billing/usage?" + query, Map.of("Authorization", "Bearer " + entry.getValue()));
                //��ǰ����
                final String total_usage = subText(usage, "\"total_usage\":", "}", -1).trim();


                Map<String, Object> item = new HashMap<>();
                item.put("system_hard_limit_usd", system_hard_limit_usd);
                item.put("total_usage", total_usage);
                updateItems.put(entry.getKey(), item);

                countDownLatch.countDown();
            });
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ����
        updateItems.entrySet().forEach((entry) -> {
            String system_hard_limit_usd = (String) entry.getValue().get("system_hard_limit_usd");
            String total_usage = (String) entry.getValue().get("total_usage");

            tableModel.setValueAt(system_hard_limit_usd, entry.getKey(), 1);
            tableModel.setValueAt(String.valueOf(Double.parseDouble(system_hard_limit_usd) - Double.valueOf(total_usage) / 100), entry.getKey(), 2);
        });

    }

    private static byte[] readFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bin = new byte[((Number) file.length()).intValue()];
            fileInputStream.read(bin);
            fileInputStream.close();
            return bin;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void loadFile(File file) {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        byte[] bin = readFile(file);
        var lines = Arrays.stream(new String(bin).split("\\r\\n|\\n")).filter(it -> it != null && it.length() > 0).collect(Collectors.toList());
        lines.forEach((it) -> {
            tableModel.addRow(new Object[]{it, "", ""});
        });
    }


    private static String get(String url, Map<String, String> headers) {
        try {
            final URI uri = URI.create(url);
            final HttpClient httpClient = HttpClient.newBuilder()
                    .build();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .uri(uri);

            headers.entrySet().forEach((entry) -> {
                builder.header(entry.getKey(), entry.getValue());
            });

            final HttpRequest request = builder.build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String subText(String source, String startText, String endText, int offSet) {
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
