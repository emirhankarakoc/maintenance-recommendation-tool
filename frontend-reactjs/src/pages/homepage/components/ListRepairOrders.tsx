"use client";

import { http } from "@/assets/http";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

type RepairOrder = {
  number: string;
  createdAt: string;
  processed: boolean;
};

export default function ListRepairOrders() {
  const [repairOrders, setRepairOrders] = useState<RepairOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  // ============================================================
  // LOAD REPAIR ORDERS
  // ============================================================

  const getRepairOrders = async (manualRefresh = false) => {
    try {
      if (manualRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      setError("");

      const response = await http.get("/repair-orders");

      const data: RepairOrder[] = Array.isArray(response.data)
        ? response.data
        : [];

      // newest RO first
      const sorted = [...data].sort(
        (a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
      );

      setRepairOrders(sorted);
    } catch (error: any) {
      console.error("Failed to get repair orders:", error);

      setError(
        error.response?.data?.message || "Failed to load repair orders.",
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    getRepairOrders();
  }, []);

  // ============================================================
  // LOADING
  // ============================================================

  if (loading) {
    return (
      <div className="w-full max-w-4xl">
        <div className="rounded-xl border p-6 text-gray-500">
          Loading repair orders...
        </div>
      </div>
    );
  }

  // ============================================================
  // PAGE
  // ============================================================

  return (
    <div className="w-full max-w-4xl">
      {/* HEADER */}

      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Repair Orders</h1>

          <p className="mt-1 text-sm text-gray-500">
            Repair orders captured by the Sofra extension.
          </p>
        </div>

        <button
          type="button"
          disabled={refreshing}
          onClick={() => getRepairOrders(true)}
          className="rounded-lg border px-4 py-2 text-sm font-bold transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {refreshing ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {/* ERROR */}

      {error && (
        <div className="mb-5 rounded-xl border border-red-500/30 bg-red-500/10 p-4 text-red-500">
          {error}
        </div>
      )}

      {/* EMPTY */}

      {!error && repairOrders.length === 0 && (
        <div className="rounded-xl border border-dashed p-8 text-center">
          <div className="text-lg font-bold">No repair orders yet</div>

          <div className="mt-1 text-sm text-gray-500">
            Run the Sofra extension on a vehicle to create an RO.
          </div>
        </div>
      )}

      {/* LIST */}

      <div className="flex flex-col gap-3">
        {repairOrders.map((ro) => (
          <Link
            key={ro.number}
            to={`/results/${encodeURIComponent(ro.number)}`}
            className="group block rounded-xl border p-5 transition hover:border-gray-400 hover:bg-gray-50 hover:shadow-sm"
          >
            <div className="flex items-center justify-between gap-4">
              <div>
                <div className="text-xl font-bold">RO #{ro.number}</div>

                <div className="mt-1 text-sm text-gray-500">
                  {ro.createdAt
                    ? new Date(ro.createdAt).toLocaleString()
                    : "Unknown date"}
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div
                  className={`rounded-full px-3 py-1 text-xs font-bold ${
                    ro.processed
                      ? "bg-green-100 text-green-700"
                      : "bg-yellow-100 text-yellow-700"
                  }`}
                >
                  {ro.processed ? "READY" : "PROCESSING"}
                </div>

                <div className="text-xl text-gray-400 transition group-hover:translate-x-1">
                  →
                </div>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
