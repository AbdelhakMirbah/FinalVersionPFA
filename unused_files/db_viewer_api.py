"""
Simple API server to fetch fraud detection records from PostgreSQL
Run this with: python3 db_viewer_api.py
"""

from flask import Flask, jsonify
from flask_cors import CORS
import psycopg2
from psycopg2.extras import RealDictCursor

app = Flask(__name__)
CORS(app)  # Enable CORS for browser access

# Database configuration
DB_CONFIG = {
    "dbname": "fraud_db",
    "user": "postgres",
    "password": "password",
    "host": "127.0.0.1",
    "port": "5433"
}

@app.route('/api/records', methods=['GET'])
def get_records():
    """Fetch all fraud check records from PostgreSQL"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        
        # Fetch all records, ordered by most recent first
        cursor.execute("""
            SELECT id, amount, score, risk, created_at 
            FROM fraud_checks 
            ORDER BY created_at DESC 
            LIMIT 50
        """)
        
        records = cursor.fetchall()
        
        # Convert to JSON-serializable format
        result = []
        for record in records:
            result.append({
                'id': record['id'],
                'amount': float(record['amount']),
                'score': float(record['score']),
                'risk': record['risk'],
                'created_at': record['created_at'].isoformat() if record['created_at'] else None
            })
        
        cursor.close()
        conn.close()
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/stats', methods=['GET'])
def get_stats():
    """Get statistics about fraud checks"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        
        cursor.execute("""
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN risk = 'HIGH' THEN 1 ELSE 0 END) as high_risk,
                SUM(CASE WHEN risk = 'LOW' THEN 1 ELSE 0 END) as low_risk,
                AVG(score) as avg_score
            FROM fraud_checks
        """)
        
        stats = cursor.fetchone()
        
        cursor.close()
        conn.close()
        
        return jsonify({
            'total': int(stats['total']) if stats['total'] else 0,
            'high_risk': int(stats['high_risk']) if stats['high_risk'] else 0,
            'low_risk': int(stats['low_risk']) if stats['low_risk'] else 0,
            'avg_score': float(stats['avg_score']) if stats['avg_score'] else 0.0
        })
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    print("=" * 60)
    print("🚀 Fraud Detection Data Viewer API")
    print("=" * 60)
    print("Starting server on http://localhost:5000")
    print("\nEndpoints:")
    print("  - GET /api/records  - Fetch all fraud check records")
    print("  - GET /api/stats    - Get statistics")
    print("  - GET /health       - Health check")
    print("\nOpen viewer.html in your browser to see the data!")
    print("=" * 60)
    
    app.run(host='0.0.0.0', port=5000, debug=True)
