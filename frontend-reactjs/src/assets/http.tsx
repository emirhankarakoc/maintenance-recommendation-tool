import axios from "axios";
import toast from "react-hot-toast";

const LOCALHOST = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
export const APIURL = LOCALHOST;

const token = localStorage.getItem("jwtToken");

export const http = axios.create({
  baseURL: APIURL,
  data: {},
  headers: token
    ? {
        Authorization: "Bearer " + token,
      }
    : {},
});

/*
 * Global API error handler
 *
 * Backend example:
 *
 * {
 *   "statusCode": 400,
 *   "message": "This RO number is already in use."
 * }
 */
http.interceptors.response.use(
  (response) => response,

  (error) => {
    let errorMessage = "Something went wrong.";

    if (error.response?.data) {
      const data = error.response.data;

      if (typeof data === "string") {
        errorMessage = data;
      } else if (data.message) {
        errorMessage = data.message;
      }
    } else if (error.message) {
      errorMessage = error.message;
    }

    toast.error(errorMessage);

    return Promise.reject(error);
  },
);

export const httpError = (error: any) => {
  if (error.response?.data) {
    const data = error.response.data;

    if (typeof data === "string") {
      return data;
    }

    if (data.message) {
      return data.message;
    }
  }

  return error.message || "Something went wrong.";
};
