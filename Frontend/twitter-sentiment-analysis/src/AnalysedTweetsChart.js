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
                            min: 0,
                            max: 100
                        }
                    }]
                }
            },
            data: {
                labels: this.props.data.map(d => d.label),
                datasets: [{
                    label: this.props.title,
                    data: this.props.data.map(d => d.value)
                }]
            }
        })
    }

    componentDidUpdate() {
        this.chart.data.labels = this.props.data.map(d => d.label)
        this.chart.data.datasets[0].data =  this.props.data.map(d => d.value)
        this.chart.update()
    }

    render() {
        return (
            <canvas ref={this.chartRef}/>
        )
    }

}

export default AnalysedTweetsChart