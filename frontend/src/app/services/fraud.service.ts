import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { FraudCheck } from '../models/fraud-check.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class FraudService {

    private apiUrl = 'http://localhost:8088/api/v1';

    constructor(private _zone: NgZone, private http: HttpClient) { }

    /**
     * Connect to SSE stream
     */
    getServerSentEvent(url: string): Observable<FraudCheck> {
        return new Observable(observer => {
            const eventSource = new EventSource(url);

            eventSource.onmessage = event => {
                this._zone.run(() => {
                    const data = JSON.parse(event.data);
                    observer.next(data);
                });
            };

            eventSource.onerror = error => {
                this._zone.run(() => {
                    observer.error(error);
                });
            };
        });
    }

    getFraudStream(): Observable<FraudCheck> {
        return this.getServerSentEvent(`${this.apiUrl}/records/stream`);
    }

    checkFraud(data: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/fraud/check`, data);
    }
}
