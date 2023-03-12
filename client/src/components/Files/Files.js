import React, { useState, useEffect } from 'react'
import FileItem from './FileItem'
import FileCard from './FileCard'
import './styles.css'

const Files = ({ files, searchTerm }) => {
    const [dynamicFiles, setDynamicFiles] = useState(files);

    useEffect(() => {
        setDynamicFiles(files);
    }, [files]);

    // Filter the files array based on whether the caption property includes the searchTerm
    const filteredFiles = dynamicFiles.filter((item) => JSON.parse(item).fileName?.toLowerCase().includes(searchTerm?.toLowerCase()));
    console.log(files);

    // console.log("Here I am printing the array: " + files);
    // console.log(files.map(obj => obj.fileName));

    return (
        <div className="fileView">
            <h5 style={{ fontSize: 20, marginBottom: '-10px', fontWeight: 'normal' }}>Recent Files</h5>

            <div className='fileView_row'>
                {
                    files.slice(0, 6).map((item, index) => (
                        <FileCard key={index} name={JSON.parse(item).fileName} className="fileCard" />
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
                    <FileItem key={index} caption={JSON.parse(item).fileName} timestamp={0} fileUrl={JSON.parse(item).bytes} size={0} />
                ))
            }

        </div>

    )
}

export default Files