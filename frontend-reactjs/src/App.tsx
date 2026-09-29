import { Route, Routes } from "react-router-dom";
import Homepage from "./pages/homepage/Homepage";
import ServicesPage from "./pages/services/ServicesPage";
import Login from "./pages/login/Login";
import Register from "./pages/register/Register";
import RunResults from "./pages/homepage/components/RunResults";
function App() {
  return (
    <Routes>
      <Route path="/" element={<Homepage />} />
      <Route path="/services" element={<ServicesPage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/results/:ronumber" element={<RunResults />} />
    </Routes>
  );
}

export default App;
