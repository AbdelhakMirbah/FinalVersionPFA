import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common'; // Import CommonModule
import { FraudService } from '../../services/fraud.service';
import { FraudCheck } from '../../models/fraud-check.model';
import { Subscription } from 'rxjs';
import { NgxChartsModule, Color, ScaleType } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms'; // Import FormsModule
import { HttpClientModule, HttpClient } from '@angular/common/http';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgxChartsModule, FormsModule, HttpClientModule], // Add HttpClientModule here
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  title = 'Real-Time Fraud Monitor';

  fraudChecks: FraudCheck[] = [];
  highRiskCount = 0;
  lowRiskCount = 0;
  streamSubscription!: Subscription;

  // Chart Data
  single: any[] = [];
  view: [number, number] = [700, 400];

  // Simulation Form Data
  simAmount = 500;
  simType = 1;

  colorScheme: Color = {
    name: 'custom',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#ef4444', '#22c55e'] // Red (High), Green (Low)
  };

  constructor(private fraudService: FraudService, private http: HttpClient) { } // Inject HttpClient

  ngOnInit() {
    this.updateChartData(); // Initialize charts
    this.loadExistingRecords(); // Load past data
    this.startStream(); // Listen for new data
  }

  loadExistingRecords() {
    this.http.get<FraudCheck[]>('http://localhost:8081/api/v1/records').subscribe({
      next: (data) => {
        console.log("Loaded existing records:", data.length);
        this.fraudChecks = data;

        // Recalculate stats
        this.highRiskCount = data.filter(r => r.risk === 'HIGH').length;
        this.lowRiskCount = data.filter(r => r.risk === 'LOW').length;
        this.updateChartData();
      },
      error: (err) => console.error("Failed to load records", err)
    });
  }

  startStream() {
    this.streamSubscription = this.fraudService.getFraudStream().subscribe({
      next: (check: FraudCheck) => {
        console.log('Received:', check);
        // Add to list (at the top)
        this.fraudChecks.unshift(check);
        // Keep only last 50
        if (this.fraudChecks.length > 50) {
          this.fraudChecks.pop();
        }

        // Update Stats (increment only)
        if (check.risk === 'HIGH') {
          this.highRiskCount++;
        } else {
          this.lowRiskCount++;
        }
        this.updateChartData();
      },
      error: (err) => console.error('SSE Error:', err)
    });
  }

  updateChartData() {
    this.single = [
      {
        "name": "High Risk",
        "value": this.highRiskCount
      },
      {
        "name": "Low Risk",
        "value": this.lowRiskCount
      }
    ];
  }

  simulateTransaction() {
    const payload = {
      "amount": this.simAmount,
      "oldBalanceOrgin": 1000,
      "newBalanceOrig": 1000 - this.simAmount,
      "type": this.simType,
      "oldBalanceDest": 0,
      "newBalanceDest": this.simAmount,
      "ip": "1.2.3.4",
      "email": "test@demo.com"
    };

    this.fraudService.checkFraud(payload).subscribe({
      next: (res) => console.log('Simulation sent', res),
      error: (err) => console.error('Simulation failed', err)
    });
  }

  ngOnDestroy() {
    if (this.streamSubscription) {
      this.streamSubscription.unsubscribe();
    }
  }
}
