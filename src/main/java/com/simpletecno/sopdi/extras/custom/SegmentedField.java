package com.simpletecno.sopdi.extras.custom;

import com.vaadin.ui.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SegmentedField extends CustomField<String> {

    private final List<TextField> segments = new ArrayList<>();
    private final int[] segmentLengths;

    private final VerticalLayout wrapper = new VerticalLayout();
    private final HorizontalLayout fieldLayout = new HorizontalLayout();

    public SegmentedField(int[] segmentLengths) {
        if (segmentLengths == null || segmentLengths.length == 0) {
            throw new IllegalArgumentException("Debe proporcionar al menos un segmento.");
        }
        this.segmentLengths = segmentLengths;
        setImmediate(true);
        buildLayout();
    }

    private void buildLayout() {
        fieldLayout.setSpacing(true);
        wrapper.setSpacing(true);

        for (int i = 0; i < segmentLengths.length; i++) {
            final int index = i;
            final int maxLength = segmentLengths[i];

            TextField tf = new TextField();
            tf.addStyleName("segmented-input");
            //tf.setMaxLength(maxLength);

            // Aproximadamente 9px por carácter monospace, ajusta según fuente
            int widthPx = (maxLength * 9) + 4;
            tf.setWidth(widthPx + "px");

            tf.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
            tf.setImmediate(true);

            tf.addTextChangeListener(event -> {
                String text = event.getText();
                if (text == null) return;

                text = text.toUpperCase();  // forzamos mayúsculas
                tf.setValue(text);          // actualizamos el campo con la versión mayúscula

                // Si es pegado completo
                if (text.length() >= totalLength() || text.contains("-")) {
                    assignFullValue(text);
                    return;
                }

                // comportamiento normal
                if (text.length() > maxLength) {
                    tf.setValue(text.substring(0, maxLength));
                }
                if (text.length() == maxLength && index < segments.size() - 1) {
                    segments.get(index + 1).focus();
                }
            });


            segments.add(tf);
            fieldLayout.addComponent(tf);

            if (i < segmentLengths.length - 1) {
                fieldLayout.addComponent(new Label("-"));
            }

        }

        wrapper.addComponent(fieldLayout);
    }

    private int totalLength() {
        return segmentLengths.length - 1 +  // guiones
                java.util.Arrays.stream(segmentLengths).sum();
    }

    private void assignFullValue(String pasted) {
        String normalized = pasted.replaceAll("[^A-Za-z0-9]", ""); // quita guiones y símbolos
        int pos = 0;

        for (int i = 0; i < segmentLengths.length; i++) {
            int len = segmentLengths[i];
            if (pos + len <= normalized.length()) {
                segments.get(i).setValue(normalized.substring(pos, pos + len));
            } else {
                segments.get(i).setValue("");
            }
            pos += len;
        }
    }


    @Override
    protected Component initContent() {
        return wrapper;
    }

    @Override
    public Class<? extends String> getType() {
        return String.class;
    }

    @Override
    public String getValue() {
        return segments.stream()
                .map(TextField::getValue)
                .collect(Collectors.joining("-"));
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);

        if (value == null || value.isEmpty()) {
            segments.forEach(tf -> tf.setValue(""));
            return;
        }

        String[] parts = value.split("-");
        for (int i = 0; i < segments.size(); i++) {
            if (i < parts.length) {
                segments.get(i).setValue(parts[i]);
            } else {
                segments.get(i).setValue("");
            }
        }
    }

    @Override
    public boolean isValid() {
        for (int i = 0; i < segments.size(); i++) {
            String val = segments.get(i).getValue();
            if (val == null || val.length() != segmentLengths[i]) {
                return false;
            }
        }
        return true;
    }
}
