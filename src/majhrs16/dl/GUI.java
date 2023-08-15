package majhrs16.dl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map.Entry;

public class GUI {
	public JFrame frame;
	public DefaultListModel<String> listModel;

    public GUI() {
        frame = new JFrame(String.format("%s - %s", Main.name, Main.version));
        frame.getContentPane().setBackground(Color.DARK_GRAY);
        frame.setLayout(new BorderLayout());

        // Crear panel lateral con listbox y botón
        JPanel sidebarPanel = new JPanel();
            sidebarPanel.setBackground(Color.DARK_GRAY);
            sidebarPanel.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();

        JList<String> listBox = new JList<>(listModel);
            listBox.setBackground(Color.DARK_GRAY);
            listBox.setForeground(Color.LIGHT_GRAY);
            listBox.setSelectionBackground(Color.BLUE);
            listBox.setSelectionForeground(Color.DARK_GRAY);

        JButton button = new JButton("Lanzar!");
            button.setBackground(new Color(173, 216, 230));
            button.setForeground(Color.GRAY);
            button.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel label = new JLabel("Selecciona la versión:");
        	label.setForeground(Color.LIGHT_GRAY);

        sidebarPanel.add(label, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(listBox), BorderLayout.CENTER);
        sidebarPanel.add(button, BorderLayout.SOUTH);

        frame.add(sidebarPanel, BorderLayout.WEST);

        // Crear panel central con textarea
        JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setBackground(Color.DARK_GRAY);
            textArea.setForeground(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.add(scrollPane, BorderLayout.CENTER);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedVersion = listBox.getSelectedValue();
                if (selectedVersion != null) {
                    Thread t = new Thread(() -> {
                    	Main.launch(selectedVersion);
                    });
                    t.setDaemon(false);
                    t.start();

                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Por favor, seleccione una version.",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                char keyChar = e.getKeyChar();
                String keyText = KeyEvent.getKeyText(keyCode);

                System.out.println("Key pressed: " + keyText + " (" + keyChar + ")");
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Main.exit();
            }
        });
    }

    public void updateListBox() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!Main.exit) {
                    listModel.clear();

                    if (Main.vm.versions.isEmpty()) {
                        listModel.addElement("Buscando versiones . . .");

                    } else {
                        for (Entry<String, String[]> entry : Main.vm.versions.entrySet()) {
                            String[] v = entry.getValue();
                            listModel.addElement(v[1]);
                        }
                    }

                    Thread.sleep(5000);
                }
                return null;
            }
        };

        worker.execute();
    }
}