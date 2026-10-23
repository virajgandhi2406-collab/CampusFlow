/**
 * CampusFlow — Productivity Insights Chart Initialization
 */

document.addEventListener('DOMContentLoaded', () => {
    const chartCanvas = document.getElementById('weeklyCompletionChart');
    if (!chartCanvas || typeof Chart === 'undefined') {
        return;
    }

    try {
        const labelsData = JSON.parse(chartCanvas.getAttribute('data-labels') || '[]');
        const countsData = JSON.parse(chartCanvas.getAttribute('data-counts') || '[]');

        const ctx = chartCanvas.getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labelsData,
                datasets: [{
                    label: 'Assignments Completed',
                    data: countsData,
                    backgroundColor: 'rgba(79, 70, 229, 0.85)',
                    hoverBackgroundColor: 'rgba(67, 56, 202, 1)',
                    borderRadius: 6,
                    borderSkipped: false,
                    maxBarThickness: 42
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor: '#1e293b',
                        padding: 10,
                        cornerRadius: 6,
                        callbacks: {
                            label: function(context) {
                                const val = context.parsed.y;
                                return `${val} assignment${val === 1 ? '' : 's'} completed`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            color: '#64748b',
                            font: {
                                family: "'Inter', sans-serif",
                                size: 12
                            }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            precision: 0,
                            color: '#64748b',
                            font: {
                                family: "'Inter', sans-serif",
                                size: 12
                            }
                        },
                        grid: {
                            color: '#f1f5f9'
                        }
                    }
                }
            }
        });
    } catch (e) {
        console.error('Failed to initialize productivity chart', e);
    }
});
