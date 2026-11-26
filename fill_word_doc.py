from docx import Document

def fill_template():
    template_path = r'g:\My Drive\ooad mini project\irctc-reservation-system\Report_Template.docx'
    output_path = r'g:\My Drive\ooad mini project\irctc-reservation-system\Report_Filled.docx'
    
    doc = Document(template_path)
    
    replacements = {
        "Title": "Title: IRCTC Railway Reservation System",
        "NAME": "NAME:\n1. Mallikarjun Reddy\n2. Nithin M\n3. N Viharika\n4. Pranav G M",
        "SRN": "SRN: [Add SRNs here]",
        "Semester  - Section": "Semester - Section: 6th Semester - [Add Sec]",
        "Faculty Name": "Faculty Name: [Add Faculty Name here]",
        "Problem Statement:": "Problem Statement:\nThe existing legacy railway reservation systems face challenges related to concurrency, real-time ticket availability, dynamic pricing, and secure payment processing. This project implements a robust, scalable, and user-friendly online railway reservation system utilizing OOAD principles. The system handles scheduling, seat allocation, user management, and ticket generation while applying appropriate design patterns.",
        "Key Features:": "Key Features:\n1. User Authentication & Roles (Admin and Passenger).\n2. Train & Route Management with quotas like Tatkal.\n3. Search & Booking Engine with real-time seat availability.\n4. Dynamic Fare Calculation based on quotas.\n5. Payment Gateway Integration (UPI, Credit Card) & E-Wallet.\n6. Automated PNR generation, PDF tickets, & Notifications (Email/SMS).\n7. Booking History, Cancellations, and Admin Dashboard.",
        "Models:": "Models:\n- User/Passenger: Manages credentials, roles, and profiles.\n- Train & TrainRouteStop: Train schedules, routes, stops, and seat capacities.\n- Station: Physical railway stations.\n- Ticket & PassengerDetail: Bookings, unique PNRs, and passenger info.\n- Payment, PassengerWallet & WalletTransaction: Transactions and wallets.\n- Refund: Handles cancellation refunds.",
        "MVC Architecture used? Yes/No": "MVC Architecture used? Yes",
        "<Explanation of principle usage in the project>": "- Single Responsibility Principle (SRP): Each class handles a single functionality (e.g., PdfGeneratorService only creates PDFs).\n- Open/Closed Principle (OCP): New payment methods or fare calculators can be added via interfaces without modifying core logic.\n- Dependency Inversion Principle (DIP): High-level modules depend on abstractions (interfaces/repositories).",
        "<Explanation of pattern usage in the project>": "- Strategy Pattern: Implemented in PaymentStrategy (UPIPayment, CreditCardPayment) to dynamically select payment logic.\n- Observer Pattern: Implemented in TicketObserver (EmailNotifier, SMSNotifier) to decouple booking from notifications.\n- Factory Pattern: Implemented in FareCalculatorFactory to instantiate the correct fare calculator based on quotas.\n- Singleton Pattern: Implemented for AuditLogger and DatabaseConnectionPool to share instances globally.",
        "https://github.com/": "https://github.com/ [Add your public Github link here]"
    }
    
    for p in doc.paragraphs:
        text = p.text.strip()
        if text in replacements:
            p.text = replacements[text]
            
    # Tables processing for Individual contributions and anything else inside tables
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                text = cell.text.strip()
                if text in replacements:
                    cell.text = replacements[text]
                    
        # Check if this is the contributions table
        if len(table.rows) > 0:
            header = [cell.text.strip() for cell in table.rows[0].cells]
            if "Name" in header and "Module worked on" in header:
                if len(table.rows) > 1:
                    table.rows[1].cells[0].text = "Mallikarjun Reddy"
                    table.rows[1].cells[1].text = "Frontend UI & Admin Module"
                    
                    row2 = table.add_row()
                    row2.cells[0].text = "Nithin M"
                    row2.cells[1].text = "Backend Booking Service & Database Design"
                    
                    row3 = table.add_row()
                    row3.cells[0].text = "N Viharika"
                    row3.cells[1].text = "Payment Gateway & Passenger Wallet"
                    
                    row4 = table.add_row()
                    row4.cells[0].text = "Pranav G M"
                    row4.cells[1].text = "Notifications, PDF Generation & Pattern Implementations"
                    
    doc.save(output_path)
    print(f"Document saved to {output_path}")

if __name__ == '__main__':
    fill_template()
