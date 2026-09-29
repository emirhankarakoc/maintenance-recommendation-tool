"use client";

import { http } from "@/assets/http";
import Navigation from "@/components/Navigation";
import { Button } from "@nextui-org/button";
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

// ============================================================
// TYPES
// ============================================================

export type Recommendation = {
  id: string;
  name: string;
  carServiceId: string;
  text: string;

  laborCost?: string | null;
  opCode?: string | null;
};

export type RunResult = {
  serviceId: string;
  serviceName: string;
  serviceType: string;

  lastMileage: number | null;
  lastDate: string | null;

  milesSinceLastService: number;
  monthsSinceLastService: number | null;

  recommendations?: Recommendation[];
};

type Ro = {
  id?: string;

  number: string;

  year?: number | null;
  make?: string | null;
  model?: string | null;

  vin?: string | null;
  currentMileage?: number | null;
  vehicleMake?: string | null;
  vehicleModel?: string | null;
  vehicleYear?: string | null;
};

type RoResultsResponse = {
  ro: Ro;
  results: RunResult[];
};

// ============================================================
// HELPERS
// ============================================================

const isDirectAdd = (serviceType?: string) => {
  return serviceType === "DIRECT_ADD";
};

const isInspection = (serviceType?: string) => {
  return serviceType === "INSPECT" || serviceType === "INSPECTION";
};

const formatMileage = (mileage: number | null | undefined) => {
  if (mileage === null || mileage === undefined) {
    return "—";
  }

  return `${mileage.toLocaleString()} mi`;
};

const normalizeResponse = (responseData: any): RoResultsResponse => {
  const ro: Ro = responseData?.ro ?? {
    number: "",
  };

  const results: RunResult[] = Array.isArray(responseData?.results)
    ? responseData.results.map((result: RunResult) => ({
        ...result,

        recommendations: Array.isArray(result.recommendations)
          ? result.recommendations
          : [],
      }))
    : [];

  return {
    ro,
    results,
  };
};

// ============================================================
// COMPONENT
// ============================================================

export default function RunResults() {
  const { ronumber } = useParams();

  const [data, setData] = useState<RoResultsResponse | null>(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const [copiedId, setCopiedId] = useState<string | null>(null);

  // ============================================================
  // LOAD
  // ============================================================

  useEffect(() => {
    const loadResults = async () => {
      try {
        setLoading(true);
        setError("");

        if (!ronumber) {
          setError("RO number not found.");
          return;
        }

        const response = await http.get(
          `/repair-orders/${encodeURIComponent(ronumber)}/results`,
        );

        console.log("RUN RESULTS RESPONSE:", response.data);

        const normalized = normalizeResponse(response.data);

        console.log("NORMALIZED:", normalized);

        setData(normalized);
      } catch (error: any) {
        console.error("Failed to load repair order:", error);

        const message =
          error?.response?.data?.message ??
          error?.message ??
          "Failed to load repair order.";

        setError(
          typeof message === "string"
            ? message
            : "Failed to load repair order.",
        );
      } finally {
        setLoading(false);
      }
    };

    loadResults();
  }, [ronumber]);

  // ============================================================
  // COPY RECOMMENDATION
  // ============================================================

  const handleCopy = async (recommendation: Recommendation) => {
    try {
      await navigator.clipboard.writeText(recommendation.text ?? "");

      setCopiedId(recommendation.id);

      window.setTimeout(() => {
        setCopiedId(null);
      }, 1200);
    } catch (error) {
      console.error("Failed to copy recommendation:", error);
    }
  };

  // ============================================================
  // LOADING
  // ============================================================

  if (loading) {
    return (
      <div>
        <Navigation />

        <main className="mx-auto max-w-7xl p-6 md:p-10">
          <div className="rounded-xl border bg-white p-5 text-sm text-gray-500">
            Loading repair order...
          </div>
        </main>
      </div>
    );
  }

  // ============================================================
  // ERROR
  // ============================================================

  if (error || !data) {
    return (
      <div>
        <Navigation />

        <main className="mx-auto max-w-7xl p-6 md:p-10">
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm font-medium text-red-700">
            {error || "Repair order not found."}
          </div>

          <Link
            to="/"
            className="text-sm font-semibold text-blue-600 hover:underline"
          >
            ← Repair Orders
          </Link>
        </main>
      </div>
    );
  }

  // ============================================================
  // SAFE DATA
  // ============================================================

  const ro = data.ro;

  const results = Array.isArray(data.results) ? data.results : [];

  // ============================================================
  // VEHICLE
  // ============================================================

  const vehicleName = [ro?.vehicleYear, ro?.vehicleMake, ro?.vehicleModel]
    .filter(Boolean)
    .join(" ");

  const hasVehicleInfo = vehicleName.length > 0;
  // ============================================================
  // COUNTS
  // ============================================================

  const directAddCount = results.filter((result) =>
    isDirectAdd(result.serviceType),
  ).length;

  const inspectionCount = results.filter((result) =>
    isInspection(result.serviceType),
  ).length;

  // ============================================================
  // PAGE
  // ============================================================

  return (
    <div>
      <Navigation />

      <main className="mx-auto max-w-7xl p-6 md:p-10">
        {/* ==================================================== */}
        {/* BACK */}
        {/* ==================================================== */}

        <div className="mb-4">
          <Link
            to="/"
            className="text-sm font-semibold text-gray-500 transition hover:text-gray-900"
          >
            ← Repair Orders
          </Link>
        </div>

        {/* ==================================================== */}
        {/* COMPACT RO HEADER */}
        {/* ==================================================== */}

        <div className="mb-6 rounded-xl border bg-white px-5 py-4 shadow-sm">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            {/* LEFT */}

            <div className="flex flex-wrap items-center gap-x-4 gap-y-2">
              <div className="text-xl font-bold text-gray-900">
                RO #{ro?.number || ronumber}
              </div>

              <>
                <div className="hidden h-5 w-px bg-gray-200 sm:block" />

                {hasVehicleInfo ? (
                  <div className="font-semibold text-gray-700">
                    {vehicleName}
                  </div>
                ) : (
                  <div className="text-sm font-semibold text-red-500">
                    Vehicle information not available
                  </div>
                )}
              </>

              {ro?.currentMileage != null && (
                <>
                  <div className="hidden h-5 w-px bg-gray-200 sm:block" />

                  <div className="text-sm font-medium text-gray-500">
                    {formatMileage(ro.currentMileage)}
                  </div>
                </>
              )}

              {ro?.vin && (
                <>
                  <div className="hidden h-5 w-px bg-gray-200 md:block" />

                  <div className="font-mono text-xs text-gray-400">
                    {ro.vin}
                  </div>
                </>
              )}
            </div>

            {/* RIGHT */}

            <div className="flex flex-wrap gap-2">
              {directAddCount > 0 && (
                <div className="rounded-full bg-green-100 px-3 py-1 text-xs font-bold text-green-700">
                  {directAddCount} Direct Add
                </div>
              )}

              {inspectionCount > 0 && (
                <div className="rounded-full bg-yellow-100 px-3 py-1 text-xs font-bold text-yellow-700">
                  {inspectionCount} Inspect
                </div>
              )}

              <div className="rounded-full bg-gray-100 px-3 py-1 text-xs font-bold text-gray-600">
                {results.length} Total
              </div>
            </div>
          </div>
        </div>

        {/* ==================================================== */}
        {/* EMPTY */}
        {/* ==================================================== */}

        {results.length === 0 && (
          <div className="rounded-xl border border-dashed bg-white p-10 text-center">
            <div className="text-lg font-bold text-gray-800">
              No services due
            </div>

            <div className="mt-1 text-sm text-gray-500">
              No recommendations were generated for this repair order.
            </div>
          </div>
        )}

        {/* ==================================================== */}
        {/* RESULTS */}
        {/* ==================================================== */}

        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {results.map((result) => {
            const directAdd = isDirectAdd(result.serviceType);

            const inspection = isInspection(result.serviceType);

            const recommendations = Array.isArray(result.recommendations)
              ? result.recommendations
              : [];

            return (
              <section
                key={result.serviceId}
                className={`overflow-hidden rounded-xl border shadow-sm ${
                  directAdd
                    ? "border-green-300 bg-green-100"
                    : inspection
                      ? "border-yellow-300 bg-yellow-100"
                      : "border-gray-200 bg-white"
                }`}
              >
                {/* ============================================ */}
                {/* SERVICE */}
                {/* ============================================ */}

                <div className="p-5">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <h2 className="text-lg font-bold text-gray-900">
                        {result.serviceName}
                      </h2>

                      <div
                        className={`mt-2 inline-flex rounded-full px-2.5 py-1 text-[11px] font-black uppercase tracking-wide ${
                          directAdd
                            ? "bg-green-600 text-white"
                            : inspection
                              ? "bg-yellow-500 text-black"
                              : "bg-gray-200 text-gray-700"
                        }`}
                      >
                        {directAdd
                          ? "Direct Add"
                          : inspection
                            ? "Inspect"
                            : result.serviceType}
                      </div>
                    </div>

                    {/* MILES SINCE */}

                    <div className="text-right">
                      <div className="text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                        Miles Since
                      </div>

                      <div className="mt-1 text-base font-bold text-gray-900">
                        {formatMileage(result.milesSinceLastService)}
                      </div>
                    </div>
                  </div>

                  {/* ========================================== */}
                  {/* SERVICE HISTORY */}
                  {/* ========================================== */}

                  <div
                    className={`mt-5 grid grid-cols-2 gap-3 rounded-lg border p-3 sm:grid-cols-4 ${
                      directAdd
                        ? "border-green-200 bg-green-50"
                        : inspection
                          ? "border-yellow-200 bg-yellow-50"
                          : "border-gray-200 bg-gray-50"
                    }`}
                  >
                    <div>
                      <div className="text-[10px] font-bold uppercase tracking-wide text-gray-500">
                        Last Mileage
                      </div>

                      <div className="mt-1 text-sm font-semibold text-gray-800">
                        {result.lastMileage != null
                          ? formatMileage(result.lastMileage)
                          : "No History"}
                      </div>
                    </div>

                    <div>
                      <div className="text-[10px] font-bold uppercase tracking-wide text-gray-500">
                        Last Date
                      </div>

                      <div className="mt-1 text-sm font-semibold text-gray-800">
                        {result.lastDate || "No History"}
                      </div>
                    </div>

                    <div>
                      <div className="text-[10px] font-bold uppercase tracking-wide text-gray-500">
                        Miles Since
                      </div>

                      <div className="mt-1 text-sm font-semibold text-gray-800">
                        {formatMileage(result.milesSinceLastService)}
                      </div>
                    </div>

                    <div>
                      <div className="text-[10px] font-bold uppercase tracking-wide text-gray-500">
                        Months Since
                      </div>

                      <div className="mt-1 text-sm font-semibold text-gray-800">
                        {result.monthsSinceLastService != null
                          ? `${result.monthsSinceLastService} mo`
                          : "—"}
                      </div>
                    </div>
                  </div>
                </div>

                {/* ============================================ */}
                {/* RECOMMENDATIONS */}
                {/* ============================================ */}

                <div
                  className={`border-t p-4 ${
                    directAdd
                      ? "border-green-300 bg-green-50/60"
                      : inspection
                        ? "border-yellow-300 bg-yellow-50/60"
                        : "border-gray-200 bg-gray-50"
                  }`}
                >
                  {recommendations.length === 0 ? (
                    <div
                      className={`rounded-lg border border-dashed p-3 text-sm ${
                        directAdd
                          ? "border-green-300 bg-green-50 text-green-700"
                          : inspection
                            ? "border-yellow-300 bg-yellow-50 text-yellow-800"
                            : "border-gray-300 bg-white text-gray-400"
                      }`}
                    >
                      No recommendation configured.
                    </div>
                  ) : (
                    <div className="flex flex-col gap-3">
                      {recommendations.map((recommendation) => (
                        <div
                          key={recommendation.id}
                          className={`rounded-lg border p-4 ${
                            directAdd
                              ? "border-green-300 bg-white"
                              : inspection
                                ? "border-yellow-300 bg-white"
                                : "border-gray-200 bg-white"
                          }`}
                        >
                          {/* HEADER */}

                          <div className="flex flex-wrap items-start justify-between gap-3">
                            <div className="font-bold text-gray-900">
                              {recommendation.name}
                            </div>

                            <div className="flex flex-wrap gap-1.5">
                              {recommendation.opCode && (
                                <span
                                  className={`rounded px-2 py-1 font-mono text-[11px] font-bold ${
                                    directAdd
                                      ? "bg-green-100 text-green-800"
                                      : inspection
                                        ? "bg-yellow-100 text-yellow-800"
                                        : "bg-gray-100 text-gray-600"
                                  }`}
                                >
                                  {recommendation.opCode}
                                </span>
                              )}

                              {recommendation.laborCost && (
                                <span
                                  className={`rounded px-2 py-1 text-[11px] font-bold ${
                                    directAdd
                                      ? "bg-green-100 text-green-800"
                                      : inspection
                                        ? "bg-yellow-100 text-yellow-800"
                                        : "bg-blue-50 text-blue-700"
                                  }`}
                                >
                                  {recommendation.laborCost} hr
                                </span>
                              )}
                            </div>
                          </div>

                          {/* TEXT */}

                          <p className="mt-3 text-sm leading-6 text-gray-700">
                            {recommendation.text}
                          </p>

                          {/* ACTIONS */}

                          <div className="mt-4 flex justify-end gap-2">
                            <Button
                              size="sm"
                              variant="flat"
                              onPress={() => handleCopy(recommendation)}
                            >
                              {copiedId === recommendation.id
                                ? "Copied"
                                : "Copy"}
                            </Button>

                            <Button size="sm" isDisabled>
                              Add to Tekion
                            </Button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </section>
            );
          })}
        </div>
      </main>
    </div>
  );
}
