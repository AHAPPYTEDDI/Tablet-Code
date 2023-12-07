using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ChangeViz : MonoBehaviour
{
    public GameObject ARSessionOrigin;
    bool hasVizStarted;
    public GameObject changeVizButton;
    public GameObject Feet;
    public bool areFeetActive;
    private int buttonNumber;
    public bool HeatVisualization;
    public bool AuraVisualization;



    // Start is called before the first frame update
    void Start()
    {
        changeVizButton.SetActive(false);
        areFeetActive = false;
        buttonNumber = 1;

    }

    // Update is called once per frame
    void Update()
    {
        hasVizStarted = ARSessionOrigin.GetComponent<TouchFloorPlanDetection>().yPositionFound;

        if (hasVizStarted)
        {
            changeVizButton.SetActive(true);
        }

    }

    public void ChangeVisualization()
    {

        switch (buttonNumber)
        {
            case 1:
                Debug.Log("Standard Visualization");
                Feet.SetActive(false);
                areFeetActive = false;
                buttonNumber = 2;
                AuraVisualization = false;
                HeatVisualization = false;

                break;
            case 2:
                Debug.Log("Aura Visualization");
                Feet.SetActive(false);
                areFeetActive = false;
                buttonNumber = 3;
                AuraVisualization = true;
                HeatVisualization = false;

                break;
            case 3:
                Debug.Log("Upper Foot Visualization");
                Feet.SetActive(true);
                areFeetActive = true;
                buttonNumber = 4;
                
                AuraVisualization = false;
                HeatVisualization = false;

                break;
            case 4:
                Debug.Log("Heat Map Visualization");
                Feet.SetActive(false);
                areFeetActive = false;
                buttonNumber = 1;
                AuraVisualization = false;
                HeatVisualization = true;
                break;

        }
    }
}


