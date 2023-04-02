import React from 'react'
import Upload from './Upload'
import Share from './Share'
import Delete from './Delete'
import './styles.css'
const Sidebar = ({ selectedFiles, currentUser, files }) => {
  return (
    <div className='sidebar'>
      <Upload userName={currentUser} files={files} />
      <Share selectedFiles={selectedFiles} currentUser={currentUser} />
      <Delete selectedFiles={selectedFiles} currentUser={currentUser} />
    </div>
  )
}

export default Sidebar