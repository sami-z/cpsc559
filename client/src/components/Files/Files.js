import React, { useState, useEffect } from 'react'
import FileItem from './FileItem'
import FileCard from './FileCard'
import './styles.css'

const Files = ({ files, searchTerm, handleSelectFile }) => {
    const [dynamicFiles, setDynamicFiles] = useState(files);

    useEffect(() => {
        setDynamicFiles(files);
    }, [files]);

    // Filter the files array based on whether the caption property includes the searchTerm
    const filteredFiles = dynamicFiles.filter((item) => item.fileName?.toLowerCase().includes(searchTerm?.toLowerCase()));
    console.log(files);

    return (
        <div className="fileView">
            <h5 style={{ fontSize: 20, marginBottom: '-10px', fontWeight: 'normal' }}>Recent Files</h5>

            <div className='fileView_row'>
                {
                    files.slice(0, 6).map((item, index) => (
                        <FileCard key={index} name={item.fileName} className="fileCard" />
                    ))
                }

            </div>
            <div className='fileView_titles'>
                <div className='fileView_title_left'>
                    <p>Name</p>
                </div>
                <div className='fileView_title_right'>
                    <p>Last Modified</p>
                    <p>File Size</p>
                </div>
            </div>
            {
                filteredFiles.map((item, index) => (
                    <FileItem key={index} caption={item.fileName} timestamp={0} size={0} onSelectFile={handleSelectFile}/>
                ))
            }

        </div>

    )
}

export default Files