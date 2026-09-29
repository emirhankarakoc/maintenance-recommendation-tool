export type ServiceType = {
  id: string;
  name: string;
  intervalMiles: number;
  intervalMonths: number | null;
  serviceType: "DIRECT_ADD" | "INSPECT";
  recommendationCount: number;
};