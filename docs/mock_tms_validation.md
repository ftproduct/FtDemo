# Mock TMS Validation Checklist

Use this list whenever a demo screen is prepared. Each stage references entities seeded in `data/mock_tms_seed.sql`. Mark items ✅ / ⚠️ per screen review.

## 1. Party & User Coverage
- Ensure party badges show `Alpha Cements (consignor)`, `SwiftLog (transporter)`, `RetailHub (consignee)`.
- Verify user persona context (role, branch, contact info) is visible or implied.
- Confirm geofence radius displays on any location card (500/1000/3000 m only).
- If a driver is referenced, show consent status and device/mode.

## 2. Order Flow (PO → SO → DO)
- Purchase Order view must show `PO-2025-00045`, PO date, value, consignee.
- Sales Order card should list `SO-2025-01011`, priority=High, ship date.
- Delivery Order list must show both DOs with shipment type (FTL/PTL), weight, volume.
- DO line breakout should specify material codes (`AC-BULK`, `AC-BAG`) plus invoice linkage.

## 3. Planning & Constraints
- Highlight constraint targets (FTL weight 90%, volume 85%, PTL weight cap 9,000 kg) in any planning or analytics module.
- Freight Order tile must expose utilization %, status, planned departure, remarks.
- Indent state must reflect awarded vs bidding, with bid deadline timestamps.

## 4. Indent & Bid Module
- Display all bids per indent, showing transporter, vehicle type, rate, status.
- Ensure awarded bid surfaces on FO + indent plus vehicle assignment badge.
- For open bidding (Indent 2), show countdown or deadline indicator.

## 5. Journey & Tracking
- Journey detail should present tracking mode (vehicle GPS), consent link, driver name/license.
- Tracking timeline must mix GPS and Fastag events with timestamps and locations.
- Map/geofence entry event should align with RetailHub NCR coordinates.
- Delay reason should appear if journey status ≠ ETA (none currently).

## 6. ePOD & Delivery Confirmation
- ePOD card must cite `photo` type, submitted by consignee user, validated by consignor user.
- Provide download link for POD artifact; show validation timestamp.
- Consignee acceptance state should be clear (validated).

## 7. Freight Invoicing & Reconciliation
- Freight invoice summary must show `FINV-2025-0901`, amount, GST, status=Under Review, payment due 21 Nov.
- Reconciliation table should expose variance (-₹2,500) and reason (short delivery).
- Credit note linkage (note id 1) needs to be navigable from dispute + invoice.

## 8. Dispute Management
- Dispute card: raised by Consignor, issue short delivery, status Under Review.
- Show association with FO/DO/invoice, plus credit note reference.
- Provide action buttons (resolve, escalate) or note tasks.

## 9. Transporter Viewpoints
- Transporter dashboard must surface active indents, awarded vehicles, pending payouts.
- Driver app mockups should align with assigned journey + consent capture.
- Vehicle compliance badges (RC/insurance/fitness expiry) should display if due within 60 days.

## 10. Data Integrity Rules
- Emails unique and formatted; phone numbers prefixed with +91.
- Weight/volume units consistent (kg / cu.m); currency default INR.
- Enum-driven fields (status, shipment type, tracking sources) must use exact schema values to avoid validation failures.

