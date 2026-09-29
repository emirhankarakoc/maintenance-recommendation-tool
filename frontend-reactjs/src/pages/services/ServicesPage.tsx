import { http } from "@/assets/http";
import { ServiceType } from "@/assets/types";
import Navigation from "@/components/Navigation";
import { Button } from "@nextui-org/button";
import { useEffect, useMemo, useState } from "react";

type RecommendationType = {
  id: string;
  name: string;
  text: string;
  carServiceId: string;
  laborCost: string;
  opCode: string;
};

type CarServiceType = ServiceType & {
  intervalMonths?: number | null;
  recommendationCount: number;
};

export default function ServicesPage() {
  const [services, setServices] = useState<CarServiceType[]>([]);

  // =========================================================
  // ADD SERVICE
  // =========================================================

  const [showAddServiceModal, setShowAddServiceModal] = useState(false);

  const [serviceName, setServiceName] = useState("");
  const [intervalMiles, setIntervalMiles] = useState("");
  const [intervalMonths, setIntervalMonths] = useState("");
  const [serviceType, setServiceType] = useState("DIRECT_ADD");

  // =========================================================
  // ADD RECOMMENDATION
  // =========================================================

  const [showAddRecModal, setShowAddRecModal] = useState(false);

  const [selectedService, setSelectedService] = useState<CarServiceType | null>(
    null,
  );

  const [recName, setRecName] = useState("");
  const [recText, setRecText] = useState("");
  const [recLaborHours, setRecLaborHours] = useState("");
  const [recOpCode, setRecOpCode] = useState("");

  // =========================================================
  // SEE RECOMMENDATIONS
  // =========================================================

  const [showRecsModal, setShowRecsModal] = useState(false);

  const [recommendations, setRecommendations] = useState<RecommendationType[]>(
    [],
  );

  // =========================================================
  // STATS
  // =========================================================

  const totalRecommendations = useMemo(() => {
    return services.reduce(
      (total, service) => total + (service.recommendationCount ?? 0),
      0,
    );
  }, [services]);

  const directAddCount = useMemo(() => {
    return services.filter((service) => service.serviceType === "DIRECT_ADD")
      .length;
  }, [services]);

  const inspectionCount = useMemo(() => {
    return services.filter((service) => service.serviceType === "INSPECT")
      .length;
  }, [services]);

  // =========================================================
  // STARTUP
  // =========================================================

  useEffect(() => {
    fetchServices();
  }, []);

  const fetchServices = async () => {
    try {
      const response = await http.get("/services");
      setServices(response.data);
    } catch (error) {
      console.error("Failed to fetch services:", error);
    }
  };

  // =========================================================
  // CREATE SERVICE
  // =========================================================

  const createService = async () => {
    if (!serviceName.trim() || !intervalMiles) {
      return;
    }

    try {
      const response = await http.post("/services", {
        name: serviceName.trim(),
        intervalMiles: Number(intervalMiles),
        intervalMonths: intervalMonths ? Number(intervalMonths) : null,
        serviceType,
      });

      setServices((old) => [...old, response.data]);

      setServiceName("");
      setIntervalMiles("");
      setIntervalMonths("");
      setServiceType("DIRECT_ADD");

      setShowAddServiceModal(false);
    } catch (error) {
      console.error("Failed to create service:", error);
    }
  };

  // =========================================================
  // OPEN ADD REC
  // =========================================================

  const openAddRecModal = (service: CarServiceType) => {
    setSelectedService(service);

    setRecName("");
    setRecText("");
    setRecLaborHours("");
    setRecOpCode("");

    setShowAddRecModal(true);
  };

  // =========================================================
  // CREATE RECOMMENDATION
  // =========================================================

  const createRecommendation = async () => {
    if (!selectedService) {
      return;
    }

    if (
      !recName.trim() ||
      !recText.trim() ||
      !recLaborHours.trim() ||
      !recOpCode.trim()
    ) {
      return;
    }

    try {
      await http.post("/recs", {
        name: recName.trim(),
        text: recText.trim(),
        carServiceId: selectedService.id,
        laborCost: recLaborHours.trim(),
        opCode: recOpCode.trim(),
      });

      setServices((oldServices) =>
        oldServices.map((service) =>
          service.id === selectedService.id
            ? {
                ...service,
                recommendationCount: (service.recommendationCount ?? 0) + 1,
              }
            : service,
        ),
      );

      setRecName("");
      setRecText("");
      setRecLaborHours("");
      setRecOpCode("");

      setSelectedService(null);
      setShowAddRecModal(false);
    } catch (error) {
      console.error("Failed to create recommendation:", error);
    }
  };

  // =========================================================
  // SEE RECOMMENDATIONS
  // =========================================================

  const seeRecommendations = async (service: CarServiceType) => {
    try {
      setSelectedService(service);

      const response = await http.get(`/recs/${service.id}`);

      setRecommendations(response.data);
      setShowRecsModal(true);
    } catch (error) {
      console.error("Failed to fetch recommendations:", error);
    }
  };

  // =========================================================
  // DELETE SERVICE
  // =========================================================

  const deleteService = async (service: CarServiceType) => {
    const confirmed = window.confirm(`Delete ${service.name}?`);

    if (!confirmed) {
      return;
    }

    try {
      await http.delete(`/services/${service.id}`);

      setServices((old) => old.filter((item) => item.id !== service.id));
    } catch (error) {
      console.error("Failed to delete service:", error);
    }
  };

  // =========================================================
  // DELETE RECOMMENDATION
  // =========================================================

  const deleteRecommendation = async (recommendation: RecommendationType) => {
    const confirmed = window.confirm(`Delete ${recommendation.name}?`);

    if (!confirmed) {
      return;
    }

    try {
      await http.delete(`/recs/${recommendation.id}`);

      setRecommendations((old) =>
        old.filter((rec) => rec.id !== recommendation.id),
      );

      if (selectedService) {
        setServices((old) =>
          old.map((service) =>
            service.id === selectedService.id
              ? {
                  ...service,
                  recommendationCount: Math.max(
                    0,
                    (service.recommendationCount ?? 0) - 1,
                  ),
                }
              : service,
          ),
        );
      }
    } catch (error) {
      console.error("Failed to delete recommendation:", error);
    }
  };

  return (
    <div className="min-h-screen bg-[#f6f7fb] text-slate-900">
      <Navigation />

      {/* ===================================================== */}
      {/* PAGE */}
      {/* ===================================================== */}

      <main className="mx-auto max-w-[1500px] px-5 py-8 lg:px-10">
        {/* HEADER */}

        <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="mb-1 text-sm font-medium text-blue-600">
              Service Configuration
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-slate-900">
              Services
            </h1>

            <p className="mt-2 text-sm text-slate-500">
              Manage maintenance intervals and service recommendations.
            </p>
          </div>

          <Button
            color="primary"
            size="lg"
            onPress={() => setShowAddServiceModal(true)}
            className="font-semibold shadow-sm"
          >
            + Add Service
          </Button>
        </div>

        {/* ===================================================== */}
        {/* STATS */}
        {/* ===================================================== */}

        <div className="mb-8 grid grid-cols-2 gap-3 lg:grid-cols-4">
          <StatCard label="Services" value={services.length} />

          <StatCard label="Recommendations" value={totalRecommendations} />

          <StatCard label="Direct Add" value={directAddCount} />

          <StatCard label="Inspection" value={inspectionCount} />
        </div>

        {/* ===================================================== */}
        {/* SERVICES */}
        {/* ===================================================== */}

        {services.length === 0 ? (
          <div className="flex min-h-[300px] flex-col items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-white">
            <div className="mb-3 text-4xl">🛠️</div>

            <h2 className="text-lg font-semibold text-slate-800">
              No services yet
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Add your first maintenance service to get started.
            </p>

            <Button
              className="mt-5"
              color="primary"
              onPress={() => setShowAddServiceModal(true)}
            >
              Add Service
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3">
            {services.map((service) => {
              const isDirectAdd = service.serviceType === "DIRECT_ADD";

              return (
                <div
                  key={service.id}
                  className="
                    group rounded-2xl border border-slate-200
                    bg-white p-5 shadow-sm transition-all
                    duration-200 hover:-translate-y-0.5
                    hover:border-slate-300 hover:shadow-md
                  "
                >
                  {/* TOP */}

                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0">
                      <h2 className="truncate text-lg font-bold text-slate-900">
                        {service.name}
                      </h2>

                      <p className="mt-1 truncate font-mono text-xs text-slate-400">
                        {service.id}
                      </p>
                    </div>

                    <span
                      className={`shrink-0 rounded-full px-3 py-1 text-xs font-semibold ${
                        isDirectAdd
                          ? "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200"
                          : "bg-amber-50 text-amber-700 ring-1 ring-amber-200"
                      }`}
                    >
                      {isDirectAdd ? "Direct Add" : "Inspection"}
                    </span>
                  </div>

                  {/* INTERVAL INFO */}

                  <div className="mt-6 grid grid-cols-2 gap-3">
                    <div className="rounded-xl bg-slate-50 px-4 py-3">
                      <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                        Mileage
                      </p>

                      <p className="mt-1 text-base font-bold text-slate-800">
                        {service.intervalMiles.toLocaleString()}
                      </p>

                      <p className="text-xs text-slate-400">miles</p>
                    </div>

                    <div className="rounded-xl bg-slate-50 px-4 py-3">
                      <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                        Time
                      </p>

                      <p className="mt-1 text-base font-bold text-slate-800">
                        {service.intervalMonths ?? "—"}
                      </p>

                      <p className="text-xs text-slate-400">
                        {service.intervalMonths ? "months" : "not set"}
                      </p>
                    </div>
                  </div>

                  {/* RECOMMENDATIONS */}

                  <div className="mt-4 flex items-center justify-between rounded-xl border border-slate-100 px-4 py-3">
                    <div>
                      <p className="text-xs text-slate-400">Recommendations</p>

                      <p className="mt-0.5 text-xl font-bold text-slate-800">
                        {service.recommendationCount ?? 0}
                      </p>
                    </div>

                    <Button
                      size="sm"
                      variant="flat"
                      color="secondary"
                      onPress={() => seeRecommendations(service)}
                    >
                      View
                    </Button>
                  </div>

                  {/* ACTIONS */}

                  <div className="mt-5 flex gap-2 border-t border-slate-100 pt-4">
                    <Button
                      color="primary"
                      variant="flat"
                      className="flex-1 font-medium"
                      onPress={() => openAddRecModal(service)}
                    >
                      + Add Rec
                    </Button>

                    <Button
                      color="danger"
                      variant="light"
                      onPress={() => deleteService(service)}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {/* ===================================================== */}
      {/* ADD SERVICE MODAL */}
      {/* ===================================================== */}

      {showAddServiceModal && (
        <ModalBackdrop>
          <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl">
            <ModalHeader
              title="Add Service"
              description="Create a new maintenance service and configure its interval."
              onClose={() => setShowAddServiceModal(false)}
            />

            <div className="space-y-4 p-6">
              <FormField label="Service Name">
                <input
                  value={serviceName}
                  onChange={(e) => setServiceName(e.target.value)}
                  placeholder="Brake Fluid Exchange"
                  className={inputClass}
                />
              </FormField>

              <div className="grid grid-cols-2 gap-4">
                <FormField label="Interval Miles">
                  <input
                    type="number"
                    value={intervalMiles}
                    onChange={(e) => setIntervalMiles(e.target.value)}
                    placeholder="30000"
                    className={inputClass}
                  />
                </FormField>

                <FormField label="Interval Months" optional>
                  <input
                    type="number"
                    value={intervalMonths}
                    onChange={(e) => setIntervalMonths(e.target.value)}
                    placeholder="24"
                    className={inputClass}
                  />
                </FormField>
              </div>

              <FormField label="Service Type">
                <select
                  value={serviceType}
                  onChange={(e) => setServiceType(e.target.value)}
                  className={inputClass}
                >
                  <option value="DIRECT_ADD">Direct Add</option>

                  <option value="INSPECTION">Inspection</option>
                </select>
              </FormField>
            </div>

            <ModalFooter>
              <Button
                variant="light"
                onPress={() => setShowAddServiceModal(false)}
              >
                Cancel
              </Button>

              <Button color="primary" onPress={createService}>
                Create Service
              </Button>
            </ModalFooter>
          </div>
        </ModalBackdrop>
      )}

      {/* ===================================================== */}
      {/* ADD RECOMMENDATION MODAL */}
      {/* ===================================================== */}

      {showAddRecModal && selectedService && (
        <ModalBackdrop>
          <div className="w-full max-w-2xl rounded-2xl bg-white shadow-2xl">
            <ModalHeader
              title="Add Recommendation"
              description={`Add a recommendation for ${selectedService.name}.`}
              onClose={() => {
                setShowAddRecModal(false);
                setSelectedService(null);
              }}
            />

            <div className="space-y-4 p-6">
              <FormField label="Recommendation Name">
                <input
                  value={recName}
                  onChange={(e) => setRecName(e.target.value)}
                  placeholder="Brake Fluid Exchange"
                  className={inputClass}
                />
              </FormField>

              <FormField label="Recommendation Text">
                <textarea
                  value={recText}
                  onChange={(e) => setRecText(e.target.value)}
                  placeholder="Brake fluid is due based on vehicle age and mileage..."
                  rows={5}
                  className={`${inputClass} resize-none`}
                />
              </FormField>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <FormField label="Labor Hours">
                  <div className="relative">
                    <input
                      type="number"
                      step="0.1"
                      min="0"
                      value={recLaborHours}
                      onChange={(e) => setRecLaborHours(e.target.value)}
                      placeholder="1.5"
                      className={`${inputClass} pr-14`}
                    />

                    <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs font-medium text-slate-400">
                      HR
                    </span>
                  </div>
                </FormField>

                <FormField label="Opcode">
                  <input
                    value={recOpCode}
                    onChange={(e) => setRecOpCode(e.target.value.toUpperCase())}
                    placeholder="BF123"
                    className={`${inputClass} font-mono uppercase`}
                  />
                </FormField>
              </div>
            </div>

            <ModalFooter>
              <Button
                variant="light"
                onPress={() => {
                  setShowAddRecModal(false);
                  setSelectedService(null);
                }}
              >
                Cancel
              </Button>

              <Button color="primary" onPress={createRecommendation}>
                Add Recommendation
              </Button>
            </ModalFooter>
          </div>
        </ModalBackdrop>
      )}

      {/* ===================================================== */}
      {/* SEE RECOMMENDATIONS MODAL */}
      {/* ===================================================== */}

      {showRecsModal && (
        <ModalBackdrop>
          <div className="flex max-h-[85vh] w-full max-w-3xl flex-col rounded-2xl bg-white shadow-2xl">
            <ModalHeader
              title="Recommendations"
              description={
                selectedService
                  ? `${selectedService.name} recommendations`
                  : undefined
              }
              onClose={() => setShowRecsModal(false)}
            />

            <div className="overflow-y-auto p-6">
              {recommendations.length === 0 ? (
                <div className="flex min-h-[200px] flex-col items-center justify-center rounded-xl border border-dashed border-slate-200">
                  <p className="font-medium text-slate-600">
                    No recommendations
                  </p>

                  <p className="mt-1 text-sm text-slate-400">
                    This service doesn't have any recommendations yet.
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {recommendations.map((rec) => (
                    <div
                      key={rec.id}
                      className="rounded-xl border border-slate-200 bg-white p-5 transition hover:border-slate-300"
                    >
                      <div className="flex items-start justify-between gap-4">
                        <div>
                          <h3 className="font-semibold text-slate-900">
                            {rec.name}
                          </h3>

                          <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate-600">
                            {rec.text}
                          </p>
                        </div>

                        {rec.opCode && (
                          <span className="shrink-0 rounded-lg bg-slate-100 px-2.5 py-1 font-mono text-xs font-semibold text-slate-600">
                            {rec.opCode}
                          </span>
                        )}
                      </div>

                      <div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-4">
                        <div className="flex gap-6">
                          <div>
                            <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">
                              Labor
                            </p>

                            <p className="mt-0.5 text-sm font-semibold text-slate-700">
                              {rec.laborCost ? `${rec.laborCost} hr` : "—"}
                            </p>
                          </div>

                          <div>
                            <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">
                              Opcode
                            </p>

                            <p className="mt-0.5 font-mono text-sm font-semibold text-slate-700">
                              {rec.opCode || "—"}
                            </p>
                          </div>
                        </div>

                        <Button
                          color="danger"
                          variant="light"
                          size="sm"
                          onPress={() => deleteRecommendation(rec)}
                        >
                          Delete
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <ModalFooter>
              <Button variant="flat" onPress={() => setShowRecsModal(false)}>
                Close
              </Button>
            </ModalFooter>
          </div>
        </ModalBackdrop>
      )}
    </div>
  );
}

// =========================================================
// SMALL UI COMPONENTS
// =========================================================

const inputClass =
  "w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10";

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm">
      <p className="text-xs font-medium text-slate-400">{label}</p>

      <p className="mt-1 text-2xl font-bold tracking-tight text-slate-900">
        {value.toLocaleString()}
      </p>
    </div>
  );
}

function FormField({
  label,
  optional,
  children,
}: {
  label: string;
  optional?: boolean;
  children: React.ReactNode;
}) {
  return (
    <div>
      <div className="mb-1.5 flex items-center justify-between">
        <label className="text-sm font-semibold text-slate-700">{label}</label>

        {optional && <span className="text-xs text-slate-400">Optional</span>}
      </div>

      {children}
    </div>
  );
}

function ModalBackdrop({ children }: { children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4 py-8 backdrop-blur-[2px]">
      {children}
    </div>
  );
}

function ModalHeader({
  title,
  description,
  onClose,
}: {
  title: string;
  description?: string;
  onClose: () => void;
}) {
  return (
    <div className="flex items-start justify-between border-b border-slate-100 px-6 py-5">
      <div>
        <h2 className="text-lg font-bold text-slate-900">{title}</h2>

        {description && (
          <p className="mt-1 text-sm text-slate-500">{description}</p>
        )}
      </div>

      <button
        onClick={onClose}
        className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
      >
        ✕
      </button>
    </div>
  );
}

function ModalFooter({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex justify-end gap-2 border-t border-slate-100 bg-slate-50/70 px-6 py-4">
      {children}
    </div>
  );
}
