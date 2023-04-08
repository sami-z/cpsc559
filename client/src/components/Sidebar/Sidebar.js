import React from 'react'
import Upload from './Upload'
import Share from './Share'
import Unshare from './Unshare'
import Delete from './Delete'
import './styles.css'
const Sidebar = ({ selectedFiles, files, currentUser }) => {
  return (
    <div className='sidebar'>
      <Upload currentUser={currentUser} files={files} />
      <Share selectedFiles={selectedFiles} currentUser={currentUser} />
      <Unshare selectedFiles={selectedFiles} currentUser={currentUser} />
      <Delete selectedFiles={selectedFiles} currentUser={currentUser} />
    </div>
  )
}

export default Sidebar