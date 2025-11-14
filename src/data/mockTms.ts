type PartyType = 'consignor' | 'transporter' | 'consignee';
type ShipmentType = 'FTL' | 'PTL';

export interface Company {
  id: number;
  name: string;
  code: string;
  partyType: PartyType;
  industry: string;
  gst: string;
  pan: string;
}

export interface Branch {
  id: number;
  companyId: number;
  name: string;
  city: string;
  state: string;
}

export interface User {
  id: number;
  companyId: number;
  branchId: number;
  role: string;
  name: string;
  email: string;
  phone: string;
}

export interface Location {
  id: number;
  companyId: number;
  branchId: number;
  name: string;
  type: 'plant' | 'warehouse' | 'delivery_point';
  city: string;
  state: string;
  pincode: string;
  geoFenceMeters: 500 | 1000 | 3000;
  lat: number;
  lng: number;
}

export interface Vehicle {
  id: number;
  transporterId: number;
  vehicleType: string;
  vehicleNumber: string;
  weightTons: number;
  volumeCuM: number;
  trackingMode: string;
}

export interface Material {
  code: string;
  description: string;
  unit: string;
}

export interface PurchaseOrder {
  number: string;
  consignorId: number;
  consigneeId: number;
  date: string;
  status: string;
  value: number;
}

export interface SalesOrder {
  number: string;
  poNumber: string;
  shipDate: string;
  priority: 'low' | 'medium' | 'high';
  status: string;
}

export interface DeliveryOrderLine {
  materialCode: string;
  description: string;
  quantity: number;
  uom: string;
  weightKg: number;
  invoiceNumber: string;
}

export interface DeliveryOrder {
  number: string;
  soNumber: string;
  type: ShipmentType;
  originId: number;
  destinationId: number;
  weightKg: number;
  volumeCuM: number;
  status: string;
  lines: DeliveryOrderLine[];
}

export interface FreightOrder {
  foId: string;
  doNumber: string;
  transportMode: ShipmentType;
  weightUtil: number;
  volumeUtil: number;
  status: string;
  plannedDeparture: string;
  indent: {
    status: string;
    bidDeadline: string;
    awardedTransporterId?: number;
    awardedVehicleId?: number;
    awardedRate?: number;
    bids: Array<{
      transporterId: number;
      vehicleType: string;
      rate: number;
      status: string;
    }>;
  };
  journey?: {
    trackingMode: string;
    driverName: string;
    consentUrl: string;
    status: string;
    startTime: string;
    eta: string;
    events: Array<{
      type: string;
      time: string;
      location: string;
      source: string;
    }>;
  };
  epod?: {
    type: string;
    submittedBy: string;
    validatedBy: string;
    submittedAt: string;
    validatedAt: string;
    documentUrl: string;
  };
  freightInvoice?: {
    number: string;
    amount: number;
    gst: number;
    status: string;
    paymentDue: string;
    reconciliationVariance: number;
    varianceReason: string;
    disputeStatus: string;
    creditNoteAmount?: number;
  };
}

export interface MockTmsData {
  companies: Company[];
  branches: Branch[];
  users: User[];
  locations: Location[];
  materials: Material[];
  vehicles: Vehicle[];
  purchaseOrders: PurchaseOrder[];
  salesOrders: SalesOrder[];
  deliveryOrders: DeliveryOrder[];
  freightOrders: FreightOrder[];
}

export const mockTmsData: MockTmsData = {
  companies: [
    {
      id: 1,
      name: 'Alpha Cements Ltd',
      code: 'ACL',
      partyType: 'consignor',
      industry: 'Cement',
      gst: '27ABCDE1234F1Z5',
      pan: 'ABCDE1234F',
    },
    {
      id: 2,
      name: 'SwiftLog Transport',
      code: 'SLT',
      partyType: 'transporter',
      industry: 'Automotive',
      gst: '29AAICS7654G1Z2',
      pan: 'AAICS7654G',
    },
    {
      id: 3,
      name: 'RetailHub Distribution',
      code: 'RHD',
      partyType: 'consignee',
      industry: 'FMCG',
      gst: '07AABCR7890H1Z7',
      pan: 'AABCR7890H',
    },
  ],
  branches: [
    { id: 1, companyId: 1, name: 'Alpha HQ', city: 'Mumbai', state: 'Maharashtra' },
    { id: 2, companyId: 1, name: 'Alpha Plant - Pune', city: 'Pune', state: 'Maharashtra' },
    { id: 3, companyId: 2, name: 'SwiftLog West', city: 'Mumbai', state: 'Maharashtra' },
    { id: 4, companyId: 2, name: 'SwiftLog Central', city: 'Nagpur', state: 'Maharashtra' },
    { id: 5, companyId: 3, name: 'RetailHub NCR', city: 'Gurgaon', state: 'Haryana' },
  ],
  users: [
    {
      id: 1,
      companyId: 1,
      branchId: 1,
      role: 'consignor_cxo',
      name: 'Niharika Menon',
      email: 'niharika.menon@alpha.com',
      phone: '+911122334455',
    },
    {
      id: 2,
      companyId: 1,
      branchId: 2,
      role: 'consignor_logistics_head',
      name: 'Kunal Iyer',
      email: 'kunal.iyer@alpha.com',
      phone: '+919820000111',
    },
    {
      id: 3,
      companyId: 1,
      branchId: 2,
      role: 'warehouse_manager',
      name: 'Ravi Kulkarni',
      email: 'ravi.kulkarni@alpha.com',
      phone: '+919767555333',
    },
    {
      id: 4,
      companyId: 1,
      branchId: 2,
      role: 'logistics_manager',
      name: 'Saanvi Rao',
      email: 'saanvi.rao@alpha.com',
      phone: '+919619888777',
    },
    {
      id: 5,
      companyId: 2,
      branchId: 3,
      role: 'transporter_head',
      name: 'Prakash Shetty',
      email: 'prakash.shetty@swiftlog.com',
      phone: '+918451223344',
    },
    {
      id: 6,
      companyId: 2,
      branchId: 4,
      role: 'transporter_manager',
      name: 'Ibrahim Khan',
      email: 'ibrahim.khan@swiftlog.com',
      phone: '+918527889900',
    },
    {
      id: 7,
      companyId: 2,
      branchId: 4,
      role: 'driver',
      name: 'Deepak Sharma',
      email: 'deepak.sharma@swiftlog.com',
      phone: '+918888112233',
    },
    {
      id: 8,
      companyId: 3,
      branchId: 5,
      role: 'consignee_head',
      name: 'Ritu Malhotra',
      email: 'ritu.malhotra@retailhub.com',
      phone: '+911244556677',
    },
    {
      id: 9,
      companyId: 3,
      branchId: 5,
      role: 'consignee_warehouse',
      name: 'Vivek Sharma',
      email: 'vivek.sharma@retailhub.com',
      phone: '+919999556688',
    },
  ],
  locations: [
    {
      id: 1,
      companyId: 1,
      branchId: 2,
      name: 'Alpha Cement Plant Pune',
      type: 'plant',
      city: 'Pune',
      state: 'Maharashtra',
      pincode: '411057',
      geoFenceMeters: 1000,
      lat: 18.62,
      lng: 73.78,
    },
    {
      id: 2,
      companyId: 1,
      branchId: 2,
      name: 'Alpha Regional Warehouse Bhiwandi',
      type: 'warehouse',
      city: 'Bhiwandi',
      state: 'Maharashtra',
      pincode: '421302',
      geoFenceMeters: 500,
      lat: 19.2812,
      lng: 73.0489,
    },
    {
      id: 3,
      companyId: 3,
      branchId: 5,
      name: 'RetailHub NCR DC',
      type: 'delivery_point',
      city: 'Gurgaon',
      state: 'Haryana',
      pincode: '122001',
      geoFenceMeters: 1000,
      lat: 28.4595,
      lng: 77.0266,
    },
  ],
  materials: [
    { code: 'AC-BULK', description: 'OPC 53 Bulk Cement', unit: 'MT' },
    { code: 'AC-BAG', description: 'OPC 53 Bag Cement', unit: 'Bag' },
  ],
  vehicles: [
    {
      id: 1,
      transporterId: 2,
      vehicleType: '32FT Trailer',
      vehicleNumber: 'MH12AB1234',
      weightTons: 32,
      volumeCuM: 70,
      trackingMode: 'vehicle_gps',
    },
    {
      id: 2,
      transporterId: 2,
      vehicleType: '14FT Truck',
      vehicleNumber: 'MH31CD5678',
      weightTons: 9,
      volumeCuM: 25,
      trackingMode: 'driver_sim',
    },
  ],
  purchaseOrders: [
    {
      number: 'PO-2025-00045',
      consignorId: 1,
      consigneeId: 3,
      date: '2025-11-01',
      status: 'confirmed',
      value: 12500000,
    },
  ],
  salesOrders: [
    {
      number: 'SO-2025-01011',
      poNumber: 'PO-2025-00045',
      shipDate: '2025-11-05',
      priority: 'high',
      status: 'released',
    },
  ],
  deliveryOrders: [
    {
      number: 'DO-2025-7001',
      soNumber: 'SO-2025-01011',
      type: 'FTL',
      originId: 1,
      destinationId: 3,
      weightKg: 28500,
      volumeCuM: 60,
      status: 'assigned',
      lines: [
        {
          materialCode: 'AC-BULK',
          description: 'OPC 53 Bulk Cement',
          quantity: 570,
          uom: 'MT',
          weightKg: 28500,
          invoiceNumber: 'INV-FTL-001',
        },
      ],
    },
    {
      number: 'DO-2025-7002',
      soNumber: 'SO-2025-01011',
      type: 'PTL',
      originId: 2,
      destinationId: 3,
      weightKg: 6500,
      volumeCuM: 12,
      status: 'open',
      lines: [
        {
          materialCode: 'AC-BAG',
          description: 'OPC 53 Bag Cement',
          quantity: 1300,
          uom: 'Bag',
          weightKg: 6500,
          invoiceNumber: 'INV-PTL-001',
        },
      ],
    },
  ],
  freightOrders: [
    {
      foId: 'FO-1',
      doNumber: 'DO-2025-7001',
      transportMode: 'FTL',
      weightUtil: 0.89,
      volumeUtil: 0.86,
      status: 'indented',
      plannedDeparture: '2025-11-05T12:00:00+05:30',
      indent: {
        status: 'awarded',
        bidDeadline: '2025-11-04T18:00:00+05:30',
        awardedTransporterId: 2,
        awardedVehicleId: 1,
        awardedRate: 165000,
        bids: [
          { transporterId: 2, vehicleType: '32FT Trailer', rate: 165000, status: 'accepted' },
        ],
      },
      journey: {
        trackingMode: 'vehicle_gps',
        driverName: 'Deepak Sharma',
        consentUrl: 'https://cdn.ftdemo.com/consents/consent-journey-1.pdf',
        status: 'en_route',
        startTime: '2025-11-05T13:00:00+05:30',
        eta: '2025-11-07T08:00:00+05:30',
        events: [
          {
            type: 'gps_ping',
            time: '2025-11-05T16:00:00+05:30',
            location: 'Aurangabad Toll, MH',
            source: 'vehicle_gps',
          },
          {
            type: 'fastag_ping',
            time: '2025-11-06T04:30:00+05:30',
            location: 'Sardar Patel Toll, Ahmedabad',
            source: 'fastag',
          },
          {
            type: 'geofence_entry',
            time: '2025-11-07T07:55:00+05:30',
            location: 'RetailHub NCR DC',
            source: 'vehicle_gps',
          },
        ],
      },
      epod: {
        type: 'photo',
        submittedBy: 'Vivek Sharma',
        validatedBy: 'Saanvi Rao',
        submittedAt: '2025-11-07T09:30:00+05:30',
        validatedAt: '2025-11-07T10:00:00+05:30',
        documentUrl: 'https://cdn.ftdemo.com/epod/epod-journey-1.jpg',
      },
      freightInvoice: {
        number: 'FINV-2025-0901',
        amount: 165000,
        gst: 29700,
        status: 'under_review',
        paymentDue: '2025-11-21',
        reconciliationVariance: -2500,
        varianceReason: 'Short delivery adjustment',
        disputeStatus: 'under_review',
        creditNoteAmount: 2500,
      },
    },
    {
      foId: 'FO-2',
      doNumber: 'DO-2025-7002',
      transportMode: 'PTL',
      weightUtil: 0.72,
      volumeUtil: 0.48,
      status: 'planned',
      plannedDeparture: '2025-11-06T14:00:00+05:30',
      indent: {
        status: 'bidding',
        bidDeadline: '2025-11-05T18:00:00+05:30',
        bids: [{ transporterId: 2, vehicleType: '14FT Truck', rate: 42000, status: 'submitted' }],
      },
    },
  ],
};

export type MockTmsDataKey = keyof MockTmsData;

export const getMockSection = <T extends MockTmsDataKey>(key: T): MockTmsData[T] =>
  mockTmsData[key];

