FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements if they exist
COPY requirements.txt* ./

# Install Python dependencies if requirements.txt exists
RUN if [ -f requirements.txt ]; then pip install --no-cache-dir -r requirements.txt; fi

# Copy application code
COPY . .

# Default command (can be overridden in docker-compose.yml)
CMD ["python", "-m", "http.server", "8000"]


