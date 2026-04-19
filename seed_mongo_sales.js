// ==============================================================
// ASPAS - MongoDB Sales Data Simulator
// Generates 30 days of randomized sales transactions.
// 
// Run using mongosh:
// mongosh mongodb://localhost:27017/aspas_nosql seed_mongo_sales.js
// ==============================================================

const dbName = 'aspas_nosql';
const dbHandle = db.getSiblingDB(dbName);

print("Clearing existing sales_transactions...");
dbHandle.sales_transactions.deleteMany({});

// Parts from the SQL seeder
const parts = [
    { num: 'SP-BRK-001', name: 'Ceramic Brake Pads', price: 1500.00 },
    { num: 'SP-BRK-002', name: 'Brake Disc Rotor', price: 3500.00 },
    { num: 'SP-FIL-001', name: 'Standard Oil Filter', price: 450.00 },
    { num: 'SP-FIL-002', name: 'Cabin Air Filter', price: 600.00 },
    { num: 'SP-LGT-001', name: 'Halogen Headlamp H4', price: 850.00 },
    { num: 'SP-LGT-002', name: 'LED Headlamp Conversion Kit', price: 4500.00 },
    { num: 'SP-ALT-001', name: 'Alternator 12V 90A', price: 5200.00 },
    { num: 'SP-BLT-001', name: 'Timing Belt Kit', price: 2100.00 },
    { num: 'SP-SUS-001', name: 'Front Shock Absorber', price: 3200.00 },
    { num: 'SP-CLT-001', name: 'Clutch Plate Assembly', price: 4800.00 },
    { num: 'SP-ENG-001', name: 'Spark Plug Iridium', price: 350.00 },
    { num: 'SP-BAT-001', name: 'Car Battery 12V 65Ah', price: 6500.00 }
];

const transactions = [];
const todayDayEnd = new Date();
todayDayEnd.setHours(23, 59, 59, 999);

let txCounter = 1;

print("Generating 30 days of random sales history...");

// Loop over the past 30 days
for (let i = 30; i >= 0; i--) {
    let currentDay = new Date(todayDayEnd);
    currentDay.setDate(todayDayEnd.getDate() - i);
    currentDay.setHours(9, 0, 0, 0); // Start of business hours (9 AM)

    // Generate random number of transactions per day (between 2 and 8)
    let dailyTxCount = Math.floor(Math.random() * 7) + 2;
    
    for (let j = 0; j < dailyTxCount; j++) {
        // Random time during the day (9 AM to 6 PM)
        let txTime = new Date(currentDay);
        txTime.setHours(9 + Math.floor(Math.random() * 9)); // Random hour between 9 and 17
        txTime.setMinutes(Math.floor(Math.random() * 60)); // Random minute
        txTime.setSeconds(Math.floor(Math.random() * 60)); // Random second
        
        // Randomly select a part
        const part = parts[Math.floor(Math.random() * parts.length)];
        
        // Random quantity sold (1 to 4 units)
        const qty = Math.floor(Math.random() * 4) + 1;
        
        // Format transaction ID logically based on date: TXN-YYYYMMDD-SEQ
        const yy = txTime.getFullYear();
        const mm = String(txTime.getMonth() + 1).padStart(2, '0');
        const dd = String(txTime.getDate()).padStart(2, '0');
        const seq = String(txCounter++).padStart(4, '0');
        const txId = `TXN-${yy}${mm}${dd}-${seq}`;
        
        // Push the formatted MongoDB document
        transactions.push({
            transactionId: txId,
            transactionDate: txTime,
            partNumber: part.num,
            partName: part.name,
            quantitySold: qty,
            sellingPrice: part.price,
            revenueAmount: qty * part.price,
            createdAt: txTime,
            _class: "com.aspas.model.document.SalesTransactionDoc" // Important for Spring Data MongoDB mapping
        });
    }
}

print(`Total Generated Documents: ${transactions.length}`);

// Insert all transactions at once
const result = dbHandle.sales_transactions.insertMany(transactions);
print(`Successfully inserted ${result.insertedIds.length} sales records into MongoDB.`);
print("MongoDB sales population complete!");
