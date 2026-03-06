# CA$H-Flow

## Introduction

**CA$H-Flow** is a browser application built with **React** and **Spring Boot**.

The goal of this application is to help organize investments and provide a clear overview of a portfolio.

**Important:**
This project is intended for **development and demonstration purposes only**.
All data is processed **unencrypted**, therefore **do not use real financial or sensitive data**.

Due to API limitations, only Cryptocurrencies, US stocks and some US ETFs can be tracked.

---

## Technologies and Services

This application uses the following technologies and services:

* **Finnhub.io** – Live market data via WebSocket
* **EODHD.com** – Historical market data via REST API
* **MongoDB** – Database
* **Recharts** – Chart visualization
  https://github.com/recharts/recharts

Many thanks to the providers for offering free access tiers for developers.

---

## Setup / Usage

### 1. Clone the repository

```bash
git clone https://github.com/joTholl/CashFlow
```

### 2. Configure environment variables in Spring 

Create the following environment variables before starting the application:

**MONGODB_URI**
Your MongoDB connection string
https://cloud.mongodb.com

**FINNHUB_API_TOKEN**
Create an account and obtain your API key:
https://finnhub.io

**EODHD_API_TOKEN**
Create an account and obtain your API key:
https://eodhd.com

---

### 3. Start the backend

Run the **Spring Boot** application.

---

### 4. Start the frontend

Start the **React** development server.

---

## Access the Application

Once both backend and frontend are running, the application will be available at:

http://localhost:5173

---

## Disclaimer

This project is a **personal development project** and is not intended for production use.
No guarantees are made regarding the correctness or security of the processed data.
