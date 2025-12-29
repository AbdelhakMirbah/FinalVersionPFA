import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FraudService } from '../../services/fraud.service';
import { FraudCheck } from '../../models/fraud-check.model';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './history.component.html'
})
export class HistoryComponent implements OnInit {

  records: FraudCheck[] = [];
  filteredRecords: FraudCheck[] = [];
  searchTerm: string = '';

  constructor(private http: HttpClient) { }

  ngOnInit() {
    this.loadRecords();
  }

  loadRecords() {
    // Ideally pagination, but for now fetch all (top 50 or implement full fetch in backend)
    // To be closer to "admin", we should ideally fetch ALL. 
    // But currently backend only exposes top 50 via GET /api/v1/records
    // Let's stick with that for now.
    this.http.get<FraudCheck[]>('http://localhost:8081/api/v1/records').subscribe({
      next: (data) => {
        this.records = data;
        this.filteredRecords = data;
      },
      error: (err) => console.error(err)
    });
  }

  filterRecords() {
    this.filteredRecords = this.records.filter(record => {
      const matchSearch = this.searchTerm ?
        (record.id?.toString().includes(this.searchTerm) ||
          record.amount.toString().includes(this.searchTerm)) : true;
      return matchSearch;
    });
  }

  exportCSV() {
    const csvContent = "data:text/csv;charset=utf-8,"
      + "ID,Amount,Score,Risk,Date\n"
      + this.filteredRecords.map(e => `${e.id},${e.amount},${e.score},${e.risk},${e.createdAt}`).join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "fraud_report.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
