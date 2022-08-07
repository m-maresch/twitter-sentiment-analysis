import React from 'react';
import Chart from 'chart.js'

class AnalysedTweetsChart extends React.Component {
    constructor(props) {
        super(props)
        this.chartRef = React.createRef()
    }

    componentDidMount() {
        this.chart = new Chart(this.chartRef.current, {
            type: 'line',
            options: {
                scales: {
                    yAxes: [{
                        ticks: {
                            min: this.props.dataMin,
                            max: this.props.dataMax
                        }
                    }]
                }
            },
            data: {
                labels: [],
                datasets: [{
                    label: this.props.label,
                    fill: false,
                    borderColor: this.props.color,
                    data: this.props.data
                }]
            }
        })
    }

    componentDidUpdate() {
        this.chart.data.labels = Array(this.props.data.length).fill().map((_, i) => '' + i)
        this.chart.data.datasets[0].data = this.props.data

        this.chart.update()
    }

    render() {
        return (
            <canvas ref={this.chartRef}/>
        )
    }

}

export default AnalysedTweetsChart