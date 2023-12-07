using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class TextColor : MonoBehaviour
{
    // Start is called before the first frame update
    void Awake()
    {
        TextMeshPro textmeshPro = GetComponent<TextMeshPro>();
        textmeshPro.color = new Color32(1, 1, 1, 1);
    }

}
