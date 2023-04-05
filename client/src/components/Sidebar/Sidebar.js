import React from 'react'
import Upload from './Upload'
import Share from './Share'
import Unshare from './Unshare'
import Delete from './Delete'
import './styles.css'
const Sidebar = ({ selectedFiles, userName, files }) => {
  return (
    <div className='sidebar'>
      <Upload userName={userName} files={files} />
      <Share selectedFiles={selectedFiles} userName={userName} />
      <Unshare selectedFiles={selectedFiles} userName={userName} />
      <Delete selectedFiles={selectedFiles} userName={userName} />
    </div>
  )
}

export default Sidebar