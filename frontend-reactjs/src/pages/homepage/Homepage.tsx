"use client";

import { http } from "@/assets/http";
import Navigation from "@/components/Navigation";
import { useEffect, useState } from "react";
import ListRepairOrders from "./components/ListRepairOrders";

export default function Homepage() {
  const [isUserLoggedIn, setisUserLoggedIn] = useState<Boolean>();

  useEffect(() => {
    const getMe = async () => {
      try {
        await http.get("/accounts/authtest");
        setisUserLoggedIn(true);
      } catch {
        setisUserLoggedIn(false);
      }
    };

    getMe();
  }, []);

  if (!isUserLoggedIn) {
    return (
      <div>
        <Navigation />
        <div>Login first - log out and login again or register.</div>
      </div>
    );
  }
  return (
    <div>
      <div>
        <Navigation />
      </div>
      <div className="flex items-center justify-center ">
        <ListRepairOrders />
      </div>
    </div>
  );
}
