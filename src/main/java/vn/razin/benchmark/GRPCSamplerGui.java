package vn.razin.benchmark;

import com.google.common.base.Strings;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.BrowseAction;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.razin.benchmark.core.ClientList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GRPCSamplerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(GRPCSamplerGui.class);
    private static final long serialVersionUID = 240L;
    private static final String WIKIPAGE = "GRPCSampler";


    private JComboBox<String> fullMethodField;
    private JButton fullMethodButton;

    private JTextArea metadataField;
    private JLabeledTextField hostField;
    private JLabeledTextField portField;
    private JLabeledTextField deadlineField;

    private JTextField protoCompilField;
    private JButton protoCompilButton;

    private JCheckBox isTLSCheckBox;

    private JSyntaxTextArea requestJsonArea;

    public GRPCSamplerGui() {
        super();
        initGui();
        initGuiValues();
    }

    @Override
    public String getLabelResource() {
        return "grpc_sampler_title"; // $NON-NLS-1$
    }

    @Override
    public String getStaticLabel() {
        return "GRPC Request Sampler";
    }

    @Override
    public TestElement createTestElement() {
        GRPCSampler sampler = new GRPCSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);

        if (element instanceof GRPCSampler) {
            GRPCSampler sampler = (GRPCSampler) element;

            sampler.setMetadata(this.metadataField.getText());
            sampler.setHost(this.hostField.getText());
            sampler.setPort(this.portField.getText());
            sampler.setFullMethod(this.fullMethodField.getSelectedItem().toString());
            sampler.setDeadline(this.deadlineField.getText());
            sampler.setProtoCompil(this.protoCompilField.getText());
            sampler.setTls(this.isTLSCheckBox.isSelected());
            sampler.setRequestJson(this.requestJsonArea.getText());
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (element instanceof GRPCSampler) {
            GRPCSampler sampler = (GRPCSampler) element;

            metadataField.setText(sampler.getMetadata());
            hostField.setText(sampler.getHost());
            portField.setText(sampler.getPort());
            fullMethodField.setSelectedItem(sampler.getFullMethod());
            deadlineField.setText(sampler.getDeadline());
            protoCompilField.setText(sampler.getProtoCompil());
            isTLSCheckBox.setSelected(sampler.isTls());
            requestJsonArea.setText(sampler.getRequestJson());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initGuiValues();
    }

    private void initGuiValues() {
        metadataField.setText("");
        hostField.setText("");
        portField.setText("");
        fullMethodField.setSelectedItem("");
        protoCompilField.setText("");
        deadlineField.setText("999999");
        isTLSCheckBox.setSelected(false);
        requestJsonArea.setText("");
    }

    private void initGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        // TOP panel
        Container topPanel = makeTitlePanel();
        add(JMeterPluginsUtils.addHelpLinkToPanel(topPanel, WIKIPAGE), BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);

        // MAIN panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(getWebServerPanel());
        mainPanel.add(getGRPCRequestPanel());
        mainPanel.add(getProtoCompil());
        mainPanel.add(getDeadline());
        mainPanel.add(getOptionConfigPanel());
        mainPanel.add(getRequestJSONPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Helper function
     */

    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row,
                            JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }

    private JPanel getRequestJSONPanel() {
        requestJsonArea = JSyntaxTextArea.getInstance(30, 50);// $NON-NLS-1$
        requestJsonArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);

        JPanel webServerPanel = new JPanel(new BorderLayout());
        webServerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("Send JSON Format With the Request")
        ));
        webServerPanel.add(JTextScrollPane.getInstance(requestJsonArea));
        return webServerPanel;
    }

    private JPanel getOptionConfigPanel() {
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("Metadata")
        ));
        metadataField = new JTextArea("Metadata:", 5,20); // $NON-NLS-1$
        metadataField.setLineWrap(true);
        metadataField.setWrapStyleWord(true);
        metadataField.setFont(new Font("Dialog", Font.PLAIN, 14));
        metadataField.setTabSize(10);

        JPanel contents = new JPanel();
        contents.add(new JScrollPane(metadataField));

        webServerPanel.add(metadataField);
        return webServerPanel;
    }

    private JPanel getDeadline(){
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("Deadline")
        ));
        deadlineField = new JLabeledTextField("Deadline:", 7); // $NON-NLS-1$
        webServerPanel.add(deadlineField);
        return webServerPanel;
    }

    private JPanel getProtoCompil(){
        int row = 0;
        JPanel protoCompilPanel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        addToPanel(protoCompilPanel, labelConstraints, 0, row,
                new JLabel("Proto Binary File: ", JLabel.RIGHT));
        addToPanel(protoCompilPanel, editConstraints, 1, row, protoCompilField = new JTextField(20));
        addToPanel(protoCompilPanel, labelConstraints, 2, row,
                protoCompilButton = new JButton("Browse..."));

        GuiBuilderHelper.strechItemToComponent(protoCompilField, protoCompilButton);

        editConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        labelConstraints.insets = new java.awt.Insets(2, 0, 0, 0);

        protoCompilButton.addActionListener(new BrowseAction(protoCompilField));

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("GRPC Proto Compiled")
        ));
        container.add(protoCompilPanel, BorderLayout.NORTH);
        return container;

    }


    private JPanel getGRPCRequestPanel() {
        JPanel requestPanel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        // Full method
        addToPanel(requestPanel, labelConstraints, 0, row, new JLabel("Full Method: ", JLabel.RIGHT));
        addToPanel(requestPanel, editConstraints, 1, row, fullMethodField = new JComboBox<>());
        fullMethodField.setEditable(true);
        addToPanel(requestPanel, labelConstraints, 2, row,
                fullMethodButton = new JButton("Listing..."));

        fullMethodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getMethods(fullMethodField);
            }
        });

        // Container
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("GRPC Request Sampler")
        ));
        container.add(requestPanel, BorderLayout.NORTH);
        return container;
    }

    private JPanel getWebServerPanel() {
        portField = new JLabeledTextField("Port Number:", 7); // $NON-NLS-1$
        hostField = new JLabeledTextField("Server Name or IP:", 32); // $NON-NLS-1$
        isTLSCheckBox = new JCheckBox("SSL/TLS");

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder("Web Server")); // $NON-NLS-1$
        webServerPanel.add(hostField);
        webServerPanel.add(portField);
        webServerPanel.add(isTLSCheckBox);
        return webServerPanel;
    }

    private void getMethods(JComboBox<String> fullMethodField) {
        if (!Strings.isNullOrEmpty(protoCompilField.getText())) {
            List<String> methods =
                    ClientList.listServices(protoCompilField.getText());

            log.info("Full Methods: " + methods.toString());
            String[] methodsArr = new String[methods.size()];
            methods.toArray(methodsArr);

            fullMethodField.setModel(new DefaultComboBoxModel<>(methodsArr));
            fullMethodField.setSelectedIndex(0);
        }
    }

}
