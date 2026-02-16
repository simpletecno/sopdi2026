package com.simpletecno.sopdi.utilerias;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.shared.ui.JavaScriptComponentState;

@JavaScript({
        "vaadin://echarts/echarts.min.js",
        "vaadin://echarts/echarts-connector.js"
})
public class EChartsComponent extends AbstractJavaScriptComponent {

    public EChartsComponent() {
        setWidth("600px");
        setHeight("360px");
    }

    public void setOptionJson(String optionJson) {
        getState().optionJson = optionJson;
    }

    @Override
    protected EChartsState getState() {
        return (EChartsState) super.getState();
    }

    public static class EChartsState extends JavaScriptComponentState {
        public String optionJson;
    }
}
