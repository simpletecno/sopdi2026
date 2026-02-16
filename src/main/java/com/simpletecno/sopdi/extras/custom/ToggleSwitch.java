package com.simpletecno.sopdi.extras.custom;

import com.vaadin.data.Property;
import com.vaadin.ui.*;

public class ToggleSwitch extends CustomComponent {

    private final GridLayout layout = new GridLayout(3, 1);
    private final Label leftLabel = new Label();
    private final Label rightLabel = new Label();
    private final CheckBox checkBox = new CheckBox();
    private final Label descriptionLabel = new Label();

    /**
     * Crear Toggle Switch
     * @param descriptionText Texto descriptivo encima del Toggle Switch (puede ser null)
     * @param leftText Texto a la izquierda del Toggle Switch
     * @param rightText Texto a la derecha del Toggle Switch
     */
    public ToggleSwitch(String descriptionText, String leftText, String rightText) {
        this(descriptionText, leftText, rightText, null);
    }

    /**
     * Crear Toggle Switch
     * @param leftText Texto a la izquierda del Toggle Switch
     * @param rightText Texto a la derecha del Toggle Switch
     */
    public ToggleSwitch(String leftText, String rightText) {
        this(null, leftText,rightText, null);
    }

    /**
     * Crear Toggle Switch
     * @param leftText Texto a la izquierda del Toggle Switch
     * @param rightText Texto a la derecha del Toggle Switch
     * @param listener Listener para el cambio de valor
     */
    public ToggleSwitch(String leftText, String rightText, Property.ValueChangeListener listener) {
        this(null, leftText,rightText, listener);
    }

    /**
     * Crear Toggle Switch
     * @param description Texto descriptivo encima del Toggle Switch (puede ser null)
     * @param captionLeft Texto a la izquierda del Toggle Switch
     * @param captionRight Texto a la derecha del Toggle Switch
     * @param listener Listener para el cambio de valor
     */
    public ToggleSwitch(String description, String captionLeft, String captionRight, Property.ValueChangeListener listener) {
        VerticalLayout root = new VerticalLayout();
        //root.setSpacing(true);

        if (description != null && !description.isEmpty()) {
            descriptionLabel.setValue(description);
            descriptionLabel.setStyleName("v-caption");
            root.addComponent(descriptionLabel);
            root.setComponentAlignment(descriptionLabel, Alignment.MIDDLE_CENTER);
        }

        layout.setSpacing(false);

        leftLabel.setValue(captionLeft);
        leftLabel.setStyleName("v-caption");

        rightLabel.setValue(captionRight);
        rightLabel.setStyleName("v-caption");

        checkBox.setCaption("");
        checkBox.setImmediate(true);
        checkBox.addStyleName("toggle-switch");
        if (listener != null) {
            checkBox.addValueChangeListener(listener);
        }

        layout.addComponent(leftLabel, 0, 0);
        layout.addComponent(checkBox, 1, 0);
        layout.addComponent(rightLabel, 2, 0);

        layout.setComponentAlignment(leftLabel, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(checkBox, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(rightLabel, Alignment.MIDDLE_CENTER);

        root.addComponent(layout);
        root.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

        setCompositionRoot(root);
    }


    public CheckBox getCheckBox() {
        return checkBox;
    }

    public boolean getValue() {
        return checkBox.getValue();
    }

    public void setValue(boolean value) {
        checkBox.setValue(value);
    }

    public void setLeftCaption(String text) {
        leftLabel.setValue(text);
    }

    public void setRightCaption(String text) {
        rightLabel.setValue(text);
    }

    public String getLeftCaption() {
        return leftLabel.getValue();
    }

    public String getRightCaption() {
        return rightLabel.getValue();
    }

    public void setCaptionText(String left, String right) {
        leftLabel.setValue(left);
        rightLabel.setValue(right);
    }

    public void setDescriptionText(String description) {
        descriptionLabel.setValue(description);
    }

}
