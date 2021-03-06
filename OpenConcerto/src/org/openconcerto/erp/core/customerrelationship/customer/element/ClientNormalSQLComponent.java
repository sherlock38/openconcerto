/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 OpenConcerto, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU General Public License Version 3
 * only ("GPL"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.gnu.org/licenses/gpl-3.0.html See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 */
 
 package org.openconcerto.erp.core.customerrelationship.customer.element;

import org.openconcerto.erp.config.ComptaPropsConfiguration;
import org.openconcerto.erp.core.common.element.BanqueSQLElement;
import org.openconcerto.erp.core.common.element.ComptaSQLConfElement;
import org.openconcerto.erp.core.common.element.NumerotationAutoSQLElement;
import org.openconcerto.erp.core.common.ui.DeviseField;
import org.openconcerto.erp.core.customerrelationship.customer.ui.AdresseClientItemTable;
import org.openconcerto.erp.core.finance.accounting.element.ComptePCESQLElement;
import org.openconcerto.erp.core.finance.payment.component.ModeDeReglementSQLComponent;
import org.openconcerto.erp.model.ISQLCompteSelector;
import org.openconcerto.erp.preferences.DefaultNXProps;
import org.openconcerto.erp.preferences.ModeReglementDefautPrefPanel;
import org.openconcerto.sql.Configuration;
import org.openconcerto.sql.element.BaseSQLComponent;
import org.openconcerto.sql.element.ElementSQLObject;
import org.openconcerto.sql.element.SQLElement;
import org.openconcerto.sql.model.SQLBackgroundTableCache;
import org.openconcerto.sql.model.SQLBase;
import org.openconcerto.sql.model.SQLField;
import org.openconcerto.sql.model.SQLRow;
import org.openconcerto.sql.model.SQLRowAccessor;
import org.openconcerto.sql.model.SQLRowValues;
import org.openconcerto.sql.model.SQLSelect;
import org.openconcerto.sql.model.SQLTable;
import org.openconcerto.sql.model.UndefinedRowValuesCache;
import org.openconcerto.sql.model.Where;
import org.openconcerto.sql.request.SQLRowItemView;
import org.openconcerto.sql.sqlobject.ElementComboBox;
import org.openconcerto.sql.sqlobject.JUniqueTextField;
import org.openconcerto.sql.sqlobject.SQLSearchableTextCombo;
import org.openconcerto.sql.sqlobject.SQLTextCombo;
import org.openconcerto.sql.sqlobject.itemview.VWRowItemView;
import org.openconcerto.ui.DefaultGridBagConstraints;
import org.openconcerto.ui.FormLayouter;
import org.openconcerto.ui.JLabelBold;
import org.openconcerto.ui.TitledSeparator;
import org.openconcerto.ui.component.ComboLockedMode;
import org.openconcerto.ui.component.ITextArea;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Client without CTech link (i.e. there's one and only table in the DB)
public class ClientNormalSQLComponent extends BaseSQLComponent {

    private int idDefaultCompteClient = 1;
    private JCheckBox checkAdrLivraison, checkAdrFacturation;
    private final SQLTable tableNum = getTable().getBase().getTable("NUMEROTATION_AUTO");
    private ElementComboBox boxPays = null;
    private final ElementComboBox boxTarif = new ElementComboBox();

    protected boolean showMdr = true;

    private ElementSQLObject componentPrincipale, componentLivraison, componentFacturation;
    private AdresseClientItemTable adresseTable = new AdresseClientItemTable();
    private JCheckBox boxGestionAutoCompte;
    private Map<SQLField, JCheckBox> mapCheckLivraison = new HashMap<SQLField, JCheckBox>();

    private JCheckBox boxAffacturage, boxComptant;
    private DeviseField fieldMontantFactMax;
    private ISQLCompteSelector compteSel;
    private SQLRowItemView textNom;
    // ITextWithCompletion textNom;
    private final ElementComboBox comboPole = new ElementComboBox();
    private final DecimalFormat format = new DecimalFormat("000");

    private final SQLTable contactTable = Configuration.getInstance().getDirectory().getElement("CONTACT").getTable();
    private ContactItemTable table;
    private final SQLRowValues defaultContactRowVals = new SQLRowValues(UndefinedRowValuesCache.getInstance().getDefaultRowValues(this.contactTable));
    private SQLRowItemView eltModeRegl;
    private JUniqueTextField textCode;
    private JLabel labelCpt;
    private ModeDeReglementSQLComponent modeReglComp;

    public ClientNormalSQLComponent(SQLElement elt) {
        super(elt);
    }

    public void addViews() {
        this.setLayout(new GridBagLayout());
        final GridBagConstraints c = new DefaultGridBagConstraints();

        // Raison sociale
        JLabel labelRS = new JLabel(getLabelFor("FORME_JURIDIQUE"));
        labelRS.setHorizontalAlignment(SwingConstants.RIGHT);
        SQLTextCombo textType = new SQLTextCombo();

        this.add(labelRS, c);
        c.gridx++;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.BOTH;
        DefaultGridBagConstraints.lockMinimumSize(textType);
        this.add(textType, c);

        // Code
        JLabel labelCode = new JLabel(getLabelFor("CODE"));
        labelCode.setHorizontalAlignment(SwingConstants.RIGHT);
        this.textCode = new JUniqueTextField();
        c.gridx++;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(labelCode, c);
        c.gridx++;
        c.weightx = 0.5;
        c.gridwidth = 1;
        DefaultGridBagConstraints.lockMinimumSize(textCode);
        this.add(this.textCode, c);
        // Nom
        JLabel labelNom = new JLabel("Nom");
        labelNom.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        this.add(labelNom, c);
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 0.5;

        final JComponent nomComp;
            nomComp = new JTextField();
        DefaultGridBagConstraints.lockMinimumSize(nomComp);
        this.add(nomComp, c);

        if (getTable().getFieldsName().contains("ID_PAYS")) {
            c.gridx++;
            c.weightx = 0;
            this.add(new JLabel(getLabelFor("ID_PAYS"), SwingConstants.RIGHT), c);
            boxPays = new ElementComboBox(true, 25);
            c.gridx++;
            c.weightx = 0.5;
            this.add(boxPays, c);
            this.addView(boxPays, "ID_PAYS");
            DefaultGridBagConstraints.lockMinimumSize(boxPays);
        }
        if (getTable().getFieldsName().contains("LOCALISATION")) {
            c.gridy++;
            c.gridx = 0;
            c.weightx = 0;
            JLabel comp2 = new JLabel(getLabelFor("LOCALISATION"));
            comp2.setHorizontalAlignment(SwingConstants.RIGHT);
            this.add(comp2, c);
            JTextField loc = new JTextField();
            c.gridx++;
            c.weightx = 1;
            // DefaultGridBagConstraints.lockMinimumSize(boxPays);
            this.add(loc, c);
            this.addView(loc, "LOCALISATION");
        }
        // Numero intracomm
        JLabel labelIntraComm = new JLabel("N° TVA");
        labelIntraComm.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        this.add(labelIntraComm, c);

        final JTextField textNumIntracomm = new JTextField(20);
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textNumIntracomm);
        this.add(textNumIntracomm, c);
        JLabel labelSIREN = new JLabel(getLabelFor("SIRET"));
        labelSIREN.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 0;
        this.add(labelSIREN, c);

        JComponent textSiren;
            textSiren = new JTextField(20);
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textSiren);
        this.add(textSiren, c);

        // Responsable
        final JLabel responsable = new JLabel(this.getLabelFor("RESPONSABLE"));
        responsable.setHorizontalAlignment(SwingConstants.RIGHT);
        final JTextField textResp = new JTextField();
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        this.add(responsable, c);
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textResp);
        this.add(textResp, c);

        final JLabel labelRIB = new JLabel(getLabelFor("RIB"));
        labelRIB.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 0;
        this.add(labelRIB, c);

        final JTextField textRib = new JTextField();
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textRib);
        this.add(textRib, c);


        // tel
        JLabel labelTel = new JLabel(this.getLabelFor("TEL"));
        labelTel.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        this.add(labelTel, c);

        final JTextField textTel = new JTextField();
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textTel);
        this.add(textTel, c);
        textTel.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                defaultContactRowVals.put("TEL_DIRECT", textTel.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                defaultContactRowVals.put("TEL_DIRECT", textTel.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                defaultContactRowVals.put("TEL_DIRECT", textTel.getText());
            }

        });

        // email
        JLabel labelMail = new JLabel("E-mail");
        labelMail.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx++;
        c.weightx = 0;
        this.add(labelMail, c);

        final JTextField textMail = new JTextField();
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textMail);
        this.add(textMail, c);

        // Portable
        JLabel labelPortable = new JLabel("N° de portable");
        labelPortable.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        this.add(labelPortable, c);

        final JTextField textPortable = new JTextField();
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textPortable);
        this.add(textPortable, c);

        // Fax
        JLabel labelFax = new JLabel("N° de fax");
        labelFax.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx++;
        c.weightx = 0;
        this.add(labelFax, c);

        final JTextField textFax = new JTextField();
        c.gridx++;
        c.weightx = 0.5;
        DefaultGridBagConstraints.lockMinimumSize(textFax);
        this.add(textFax, c);

        textFax.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                defaultContactRowVals.put("FAX", textFax.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                defaultContactRowVals.put("FAX", textFax.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                defaultContactRowVals.put("FAX", textFax.getText());
            }

        });


        // Champ Module
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final JPanel addP = ComptaSQLConfElement.createAdditionalPanel();
        this.setAdditionalFieldsPanel(new FormLayouter(addP, 2));
        this.add(addP, c);

        c.gridy++;
        c.gridwidth = 1;

        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Adresses", createAdressesComponent());
        tabs.addTab("Contacts", createContactComponent());
        JPanel pReglement = createReglementComponent();
        if (showMdr) {
            tabs.addTab("Mode de règlement", pReglement);
        }

        tabs.addTab("Comptabilité", createComptabiliteComponent());

        tabs.setMinimumSize(new Dimension(tabs.getPreferredSize().width, tabs.getPreferredSize().height));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(tabs, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weighty = 0;


        // Mode de régelement

        if (getTable().getFieldsName().contains("ID_TARIF")) {

            // Tarif
            JLabel tarifSep = new JLabel("Tarif spécial à appliquer");
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridy++;
            c.gridx = 0;
            this.add(tarifSep, c);

            c.gridy++;
            c.gridx = 0;
            c.gridwidth = 1;
            c.weightx = 0;
            this.add(new JLabel(getLabelFor("ID_TARIF"), SwingConstants.RIGHT), c);
            c.gridx++;
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;

            this.add(boxTarif, c);
            this.addView(boxTarif, "ID_TARIF");
        }
        if (getTable().getFieldsName().contains("ID_LANGUE")) {
            // Tarif
            JLabel langueSep = new JLabel("Langue à appliquer sur les documents");
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridy++;
            c.gridx = 0;
            this.add(langueSep, c);

            c.gridy++;
            c.gridx = 0;
            c.gridwidth = 1;
            c.weightx = 0;
            this.add(new JLabel(getLabelFor("ID_LANGUE"), SwingConstants.RIGHT), c);
            c.gridx++;
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            final ElementComboBox boxLangue = new ElementComboBox();
            this.add(boxLangue, c);
            this.addView(boxLangue, "ID_LANGUE");

            boxPays.addValueListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    SQLRow row = boxPays.getSelectedRow();
                    if (row != null) {
                        boxTarif.setValue(row.getInt("ID_TARIF"));
                        boxLangue.setValue(row.getInt("ID_LANGUE"));
                    }
                }
            });
        }
        // Add on
        final JPanel addOnPanel = getAddOnPanel(this);
        if (addOnPanel != null) {
            c.gridy++;
            this.add(addOnPanel, c);
        }

        // Infos
        JLabel infosSep = new JLabel(getLabelFor("INFOS"));
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridy++;
        c.gridx = 0;
        this.add(infosSep, c);
        ITextArea textInfos = new ITextArea();
        c.gridy++;
        c.weighty = 0.3;
        c.fill = GridBagConstraints.BOTH;
        this.add(textInfos, c);

        this.checkAdrLivraison.addActionListener(new ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean b = checkAdrLivraison.isSelected();

                componentLivraison.setEditable(!b);
                componentLivraison.setCreated(!b);
            };
        });

        this.checkAdrFacturation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                boolean b = checkAdrFacturation.isSelected();

                componentFacturation.setEditable(!b);

                componentFacturation.setCreated(!b);
            }
        });

        this.addSQLObject(textType, "FORME_JURIDIQUE");
        this.addView(nomComp, "NOM", REQ);
        this.textNom = this.getView(nomComp);
        this.addSQLObject(this.textCode, "CODE");
        this.addSQLObject(textFax, "FAX");
        this.addSQLObject(textSiren, "SIRET");
        this.addSQLObject(textMail, "MAIL");
        this.addSQLObject(textTel, "TEL");
        this.addSQLObject(textPortable, "TEL_P");
        this.addSQLObject(textNumIntracomm, "NUMERO_TVA");
        this.addSQLObject(textResp, "RESPONSABLE");
        this.addSQLObject(textInfos, "INFOS");
        this.addSQLObject(this.compteSel, "ID_COMPTE_PCE");

        this.checkAdrFacturation.setSelected(true);
        this.checkAdrLivraison.setSelected(true);

    }

    private Component createAdressesComponent() {
        final JTabbedPane tabbedAdresse = new JTabbedPane() {
            public void insertTab(String title, Icon icon, Component component, String tip, int index) {
                if (component instanceof JComponent) {
                    ((JComponent) component).setOpaque(false);
                }
                super.insertTab(title, icon, component, tip, index);
            }

        };
        final GridBagConstraints c = new DefaultGridBagConstraints();
        // Adr principale
        this.addView("ID_ADRESSE", REQ + ";" + DEC + ";" + SEP);
        this.componentPrincipale = (ElementSQLObject) this.getView("ID_ADRESSE");
        this.componentPrincipale.setOpaque(false);
        tabbedAdresse.add(getLabelFor("ID_ADRESSE"), this.componentPrincipale);
        tabbedAdresse.setOpaque(false);
        // Adr facturation
        JPanel panelFacturation = new JPanel(new GridBagLayout());
        panelFacturation.setOpaque(false);
        GridBagConstraints cPanelF = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 1, 2, 1), 0, 0);

        this.addView("ID_ADRESSE_F", DEC + ";" + SEP);
        this.componentFacturation = (ElementSQLObject) this.getView("ID_ADRESSE_F");
        this.componentFacturation.setOpaque(false);
        this.componentFacturation.setCreatedUIVisible(false);
        panelFacturation.add(this.componentFacturation, cPanelF);
        this.checkAdrFacturation = new JCheckBox("Adresse de facturation identique à la principale");
        this.checkAdrFacturation.setOpaque(false);
        cPanelF.gridy++;
        panelFacturation.add(this.checkAdrFacturation, cPanelF);
            tabbedAdresse.add(getLabelFor("ID_ADRESSE_F"), panelFacturation);
            Set<SQLField> fieldsAdr = getTable().getForeignKeys("ADRESSE");
            List<SQLField> fieldsAdrOrder = new ArrayList<SQLField>(fieldsAdr);
            Collections.sort(fieldsAdrOrder, new Comparator<SQLField>() {
                @Override
                public int compare(SQLField o1, SQLField o2) {

                    return o1.getName().compareTo(o2.getName());
                }
            });
            int val = 1;
            for (SQLField sqlField : fieldsAdrOrder) {

                final String fieldName = sqlField.getName();
                if (fieldName.startsWith("ID_ADRESSE_L")) {
                    // Adr livraison
                    JPanel panelLivraison = new JPanel(new GridBagLayout());
                    panelLivraison.setOpaque(false);
                    GridBagConstraints cPanelL = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 1, 2, 1), 0, 0);

                    this.addView(fieldName, DEC + ";" + SEP);
                    this.componentLivraison = (ElementSQLObject) this.getView(fieldName);
                    this.componentLivraison.setOpaque(false);
                    this.componentLivraison.setCreatedUIVisible(false);

                    panelLivraison.add(this.componentLivraison, cPanelL);

                    checkAdrLivraison = new JCheckBox("Adresse de livraison identique à l'adresse principale");
                    checkAdrLivraison.setOpaque(false);
                    cPanelL.gridy++;
                    panelLivraison.add(checkAdrLivraison, cPanelL);
                    tabbedAdresse.add(getLabelFor(fieldName) + (val == 1 ? "" : " " + val), panelLivraison);
                    val++;

                    checkAdrLivraison.addActionListener(new ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            boolean b = checkAdrLivraison.isSelected();

                            componentLivraison.setEditable(!b);
                            componentLivraison.setCreated(!b);
                        }
                    });
                    checkAdrLivraison.setSelected(true);
                    this.mapCheckLivraison.put(sqlField, checkAdrLivraison);
                }
            }

        String labelAdrSuppl = "Adresses supplémentaires";
        tabbedAdresse.add(labelAdrSuppl, this.adresseTable);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;

        return tabbedAdresse;
    }

    private JPanel createContactComponent() {

        this.table = new ContactItemTable(this.defaultContactRowVals);
        this.table.setPreferredSize(new Dimension(this.table.getSize().width, 150));
        this.table.setOpaque(false);
        return table;
    }

    private JPanel createReglementComponent() {

        this.addView("ID_MODE_REGLEMENT", REQ + ";" + DEC + ";" + SEP);
        this.eltModeRegl = this.getView("ID_MODE_REGLEMENT");

        final JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new GridBagLayout());
        final GridBagConstraints c = new DefaultGridBagConstraints();
        final ElementSQLObject comp = (ElementSQLObject) this.eltModeRegl.getComp();
        this.modeReglComp = (ModeDeReglementSQLComponent) comp.getSQLChild();

        final JLabelBold label = new JLabelBold(getLabelFor("ID_MODE_REGLEMENT"));
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;
        c.gridy++;
        c.gridx = 0;
        p.add(label, c);
        c.gridy++;
        c.gridx = 0;
        // FIXME: comp?
        comp.setOpaque(false);
        p.add(comp, c);
        return p;
    }

    private Component createComptabiliteComponent() {
        final JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new GridBagLayout());
        final GridBagConstraints c = new DefaultGridBagConstraints();
        // Compte associé
        this.compteSel = new ISQLCompteSelector(true);
        this.boxGestionAutoCompte = new JCheckBox("Gestion Automatique des comptes");
        JLabelBold sepCompte = new JLabelBold("Compte associé");
        this.labelCpt = new JLabel(getLabelFor("ID_COMPTE_PCE"));

        if (!Boolean.valueOf(DefaultNXProps.getInstance().getProperty("HideCompteClient"))) {

            c.gridx = 0;
            c.gridy++;
            c.weightx = 1;
            c.weighty = 0;
            c.gridwidth = GridBagConstraints.REMAINDER;

            p.add(sepCompte, c);

            c.gridwidth = 1;
            c.gridy++;
            c.gridx = 0;
            c.weightx = 0;
            p.add(this.labelCpt, c);

            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridx++;
            c.weightx = 1;

            p.add(this.compteSel, c);

            this.boxGestionAutoCompte.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    setCompteVisible(!(boxGestionAutoCompte.isSelected() && getSelectedID() <= 1));
                }
            });
        }
        return p;
    }

    private void setCompteVisible(boolean b) {

        this.labelCpt.setVisible(b);
        this.compteSel.setVisible(b);
    }

    @Override
    public void update() {
        super.update();
        final int selectedID = getSelectedID();
        this.table.updateField("ID_CLIENT", selectedID);
        this.adresseTable.updateField("ID_CLIENT", selectedID);
        if (this.boxGestionAutoCompte.isSelected()) {

            SQLRow row = getTable().getRow(selectedID);
            if (row.getInt("ID_COMPTE_PCE") <= 1) {
                createCompteClientAuto(selectedID);
            } else {
                SQLRow rowCpt = row.getForeignRow("ID_COMPTE_PCE");
                String num = rowCpt.getString("NUMERO");
                String initialClient = "";
                final String text = getNameValue();
                if (text != null && text.trim().length() > 1) {
                    initialClient += text.trim().toUpperCase().charAt(0);
                }

                String compte = "411" + initialClient;
                if (!num.startsWith(compte)) {
                    int answer = JOptionPane.showConfirmDialog(null, "Voulez vous changer le compte associé au client, le nom a changé?", "Modification compte client", JOptionPane.YES_NO_OPTION);

                    if (answer == JOptionPane.YES_OPTION) {
                        createCompteClientAuto(selectedID);
                    }
                }
            }

        }
    }

    private String getNameValue() {
        return (String) ((VWRowItemView<?>) this.textNom).getWrapper().getValue();
    }

    @Override
    public void select(SQLRowAccessor r) {

        super.select(r);

        for (SQLField f : this.mapCheckLivraison.keySet()) {
            this.mapCheckLivraison.get(f).setSelected(r == null || !r.getFields().contains(f.getName()) || r.isForeignEmpty(f.getName()));
        }
        this.checkAdrFacturation.setSelected(r == null || !r.getFields().contains("ID_ADRESSE_F") || r.isForeignEmpty("ID_ADRESSE_F"));

        if (r != null) {
            this.table.insertFrom("ID_CLIENT", r.asRowValues());
            this.adresseTable.insertFrom("ID_CLIENT", r.getID());
            this.defaultContactRowVals.put("TEL_DIRECT", r.getString("TEL"));
            this.defaultContactRowVals.put("FAX", r.getString("FAX"));
        }
    }

    private void createCompteClientAuto(int idClient) {
        SQLRowValues rowVals = getTable().getRow(idClient).createEmptyUpdateRow();
        String initialClient = "";
        final String text = getNameValue();
        if (text != null && text.trim().length() > 1) {
            initialClient += text.trim().toUpperCase().charAt(0);
        }

        String compte = "411" + initialClient;

        SQLTable table = Configuration.getInstance().getDirectory().getElement("COMPTE_PCE").getTable();
        SQLSelect selCompte = new SQLSelect();
        selCompte.addSelectFunctionStar("COUNT");
        selCompte.setArchivedPolicy(SQLSelect.BOTH);
        selCompte.setWhere(new Where(table.getField("NUMERO"), "LIKE", compte + "%"));
        System.err.println(selCompte.asString());
        Object o = Configuration.getInstance().getBase().getDataSource().executeScalar(selCompte.asString());

        int nb = 0;
        if (o != null) {
            Long i = (Long) o;
            nb = i.intValue();
        }

        int idCpt = ComptePCESQLElement.getId(compte + this.format.format(nb), text);
        rowVals.put("ID_COMPTE_PCE", idCpt);
        try {
            rowVals.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int insert(SQLRow order) {

        // incrémentation du numéro auto
        if (NumerotationAutoSQLElement.getNextNumero(ClientNormalSQLElement.class, new Date()).equalsIgnoreCase(this.textCode.getText().trim())) {
            SQLRowValues rowVals = new SQLRowValues(this.tableNum);
            int val = this.tableNum.getRow(2).getInt("CLIENT_START");
            val++;
            rowVals.put("CLIENT_START", new Integer(val));

            try {
                rowVals.update(2);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        int id = super.insert(order);

        this.table.updateField("ID_CLIENT", id);
        this.adresseTable.updateField("ID_CLIENT", id);
        if (this.boxGestionAutoCompte.isSelected()) {
            createCompteClientAuto(id);
        }
        return id;
    }

    @Override
    protected SQLRowValues createDefaults() {
        SQLRowValues vals = new SQLRowValues(this.getTable());
        SQLRow r;

        vals.put("MARCHE_PUBLIC", Boolean.TRUE);
        vals.put("CODE", NumerotationAutoSQLElement.getNextNumero(ClientNormalSQLElement.class, new Date()));

        // Mode de règlement par defaut
        try {
            r = ModeReglementDefautPrefPanel.getDefaultRow(true);
            SQLElement eltModeReglement = Configuration.getInstance().getDirectory().getElement("MODE_REGLEMENT");
            if (r.getID() > 1) {
                SQLRowValues rowVals = eltModeReglement.createCopy(r, null);
                System.err.println(rowVals.getInt("ID_TYPE_REGLEMENT"));
                vals.put("ID_MODE_REGLEMENT", rowVals);
            }
        } catch (SQLException e) {
            System.err.println("Impossible de sélectionner le mode de règlement par défaut du client.");
            e.printStackTrace();
        }

        // Select Compte client par defaut
        final SQLBase base = ((ComptaPropsConfiguration) Configuration.getInstance()).getSQLBaseSociete();
        final SQLTable tablePrefCompte = base.getTable("PREFS_COMPTE");
        final SQLRow rowPrefsCompte = SQLBackgroundTableCache.getInstance().getCacheForTable(tablePrefCompte).getRowFromId(2);

        this.idDefaultCompteClient = rowPrefsCompte.getInt("ID_COMPTE_PCE_CLIENT");
        if (this.idDefaultCompteClient <= 1) {
            try {
                this.idDefaultCompteClient = ComptePCESQLElement.getIdComptePceDefault("Clients");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        vals.put("ID_COMPTE_PCE", this.idDefaultCompteClient);

        return vals;
    }

    public ContactItemTable getContactTable() {
        return this.table;
    }

    protected JPanel getAddOnPanel(BaseSQLComponent c) {
        return null;
    }
}
