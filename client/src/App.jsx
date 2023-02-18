import './App.css';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/sidebar"

function App() {
  return (
    <div className="App">
      <Navbar/>
      <Sidebar/>
      <Files/>
    </div>
  );
}

export default App;
