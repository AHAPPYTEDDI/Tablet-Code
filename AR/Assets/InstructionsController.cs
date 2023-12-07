using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class InstructionsController : MonoBehaviour
{
    public TextMeshProUGUI instructions;
    public TextMeshProUGUI buttonChange;
    public bool panelOn = true;
    public GameObject instructionsPanel;

    public GameObject NextButton;
    public GameObject BeginButton;

    // Start is called before the first frame update
    void Start()
    {
        NextButton.SetActive(true);
        BeginButton.SetActive(false);
        instructions = instructions.GetComponent<TextMeshProUGUI>();
    }

    public void NextPushed()
    {
        NextButton.SetActive(false);
        BeginButton.SetActive(true);
        instructions.text = "Touch the ground with the camera facing forward to begin the training.";
        buttonChange.text = "Begin";
    }

    public void Begin()
    {
        panelOn = false;
        instructionsPanel.SetActive(false);


    }


}
