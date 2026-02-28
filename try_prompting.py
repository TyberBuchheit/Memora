from Modules.handle_input import handle_prompt

response1 = handle_prompt(
        {
            'data':{
                'conv_id':'0ff0fb94a4e64f05a25bed428e3ffc06',
                'prompt':'Hello.'
            }
        }
    )

response2 = handle_prompt(
        {
            'data':{
                'conv_id':'1ad1dab52a414d3b9bd0f5289fb58f78',
                'prompt':'Hello.'
            }
        }
    )

response3 = handle_prompt(
        {
            'data':{
                'conv_id':'09ab6783163540b2af5ceb37d9f6d673',
                'prompt':'Hello.'
            }
        }
    )

response4 = handle_prompt(
        {
            'data':{
                'conv_id':'46da1b9c389542f0a57890d13b0ad00a',
                'prompt':'Hello.'
            }
        }
    )

response5 = handle_prompt(
        {
            'data':{
                'conv_id':'305515fe236c423e9dc1b055f4c6562c',
                'prompt':'Hello.'
            }
        }
    )

print(response1)
print(response2)
print(response3)
print(response4)
print(response5)

