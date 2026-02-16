window.com_simpletecno_sopdi_utilerias_EChartsComponent = function () {
    var element = this.getElement();
    var chart = null;

    function ensureChart() {
        if (!chart) {
            chart = echarts.init(element);
        }
    }

    function applyState(state) {
        if (!state || !state.optionJson) return;

        ensureChart();

        try {
            var option = JSON.parse(state.optionJson);
            chart.setOption(option, true);
            chart.resize();
        } catch (e) {
            // Si hay error de JSON, lo verás en consola
            console.error("ECharts optionJson inválido:", e);
        }
    }

    // Se llama cuando cambia el state desde el servidor
    this.onStateChange = function () {
        applyState(this.getState());
    };

    // Maneja resize (Vaadin 7 no siempre dispara esto perfecto, pero ayuda)
    window.addEventListener("resize", function () {
        if (chart) chart.resize();
    });
};
