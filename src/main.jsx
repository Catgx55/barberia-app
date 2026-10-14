import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import AgendarCita from './components/AgendarCita.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AgendarCita />
  </StrictMode>,
)
