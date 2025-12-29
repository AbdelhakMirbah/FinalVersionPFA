export class FraudCheck {
    id?: number; // Optional because new checks don't have IDs yet
    amount: number;
    score: number;
    risk: 'HIGH' | 'LOW';
    createdAt?: string;

    // Detailed fields
    transactionType?: number;
    oldBalance?: number;
    newBalance?: number;
    oldBalanceDest?: number;
    newBalanceDest?: number;
    ipAddress?: string;
    email?: string;

    constructor(amount: number, score: number, risk: 'HIGH' | 'LOW') {
        this.amount = amount;
        this.score = score;
        this.risk = risk;
    }
}
