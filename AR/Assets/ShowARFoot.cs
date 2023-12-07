using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ShowARFoot : MonoBehaviour
{

    public GameObject footModel;

    [SerializeField]
    private GameObject instructionPanel;

    private bool instructionPanelActive;
    // Start is called before the first frame update
    void Start()
    {
        footModel.SetActive(false);
    }

    // Update is called once per frame
    void Update()
    {
        instructionPanelActive = instructionPanel.GetComponent<InstructionsController>().panelOn;
        if (instructionPanelActive == false)
        {
            footModel.SetActive(true);
        }
    }
}
